import { existsSync, mkdirSync, writeFileSync } from "node:fs";
import path from "node:path";

const JAVA_TYPE_IMPORTS = new Map([
  ["BigDecimal", "java.math.BigDecimal"],
  ["LocalDate", "java.time.LocalDate"],
  ["LocalDateTime", "java.time.LocalDateTime"],
  ["OffsetDateTime", "java.time.OffsetDateTime"],
  ["Instant", "java.time.Instant"],
  ["UUID", "java.util.UUID"],
]);

export function generateBackend(options, backendRoot = process.cwd()) {
  const basePackage = options.basePackage ?? "com.example.governanceportal";
  const packageName = options.packageName ?? toPackageName(options.feature);
  const className = options.className ?? toPascalCase(options.feature);
  const tableName = requiredString(options.tableName, "backend.tableName");
  const idColumn = requiredString(options.idColumn, "backend.idColumn");
  const apiPath = requiredString(options.apiPath, "apiPath");
  const fields = normalizeFields(options.fields);
  const keywordColumns = normalizeKeywordColumns(options.keywordColumns, fields);
  const force = options.force === true;
  const dryRun = options.dryRun === true;

  validateJavaPackage(basePackage, "backend.basePackage");
  validateJavaPackageSegment(packageName, "backend.packageName");
  validateJavaType(className, "backend.className");
  validateSqlIdentifier(tableName, "backend.tableName");
  validateSqlIdentifier(idColumn, "backend.idColumn");

  const apiBasePath = toApiBasePath(apiPath);
  const packagePath = path.join(...basePackage.split("."), packageName);
  const sourceRoot = path.join(backendRoot, "src", "main", "java");
  const featureRoot = path.join(sourceRoot, packagePath);
  const context = {
    basePackage,
    packageName,
    packagePrefix: `${basePackage}.${packageName}`,
    className,
    variableName: toCamelCase(className),
    tableName,
    idColumn,
    apiBasePath,
    fields,
    keywordColumns,
  };

  const files = [
    {
      path: path.join(featureRoot, "api", `${className}Controller.java`),
      content: controllerTemplate(context),
    },
    {
      path: path.join(featureRoot, "service", `${className}Service.java`),
      content: serviceTemplate(context),
    },
    {
      path: path.join(featureRoot, "repository", `${className}Repository.java`),
      content: repositoryTemplate(context),
    },
    {
      path: path.join(featureRoot, "dto", `${className}Item.java`),
      content: itemTemplate(context),
    },
    {
      path: path.join(featureRoot, "dto", `${className}ListRequest.java`),
      content: listRequestTemplate(context),
    },
    {
      path: path.join(featureRoot, "dto", `${className}SearchFilter.java`),
      content: searchFilterTemplate(context),
    },
  ];

  for (const file of files) {
    if (existsSync(file.path) && !force) {
      throw new Error(`Refusing to overwrite existing file: ${relative(backendRoot, file.path)}. Use --force to replace generated files.`);
    }
  }

  if (!dryRun) {
    for (const file of files) {
      mkdirSync(path.dirname(file.path), { recursive: true });
      writeFileSync(file.path, file.content, "utf8");
    }
  }

  return {
    dryRun,
    apiBasePath,
    className,
    packageName,
    files: files.map((file) => relative(backendRoot, file.path)),
  };
}

export function toPascalCase(value) {
  return value
    .split(/[-_\s]+/)
    .filter(Boolean)
    .map((part) => `${part.charAt(0).toUpperCase()}${part.slice(1)}`)
    .join("");
}

function normalizeFields(fields) {
  if (!Array.isArray(fields) || fields.length === 0) {
    throw new Error("Field \"backend.fields\" must contain at least one field.");
  }

  return fields.map((field, index) => {
    if (!field || typeof field !== "object") {
      throw new Error(`Field "backend.fields[${index}]" must be an object.`);
    }

    const name = requiredString(field.name, `backend.fields[${index}].name`);
    const type = requiredString(field.type, `backend.fields[${index}].type`);
    const column = requiredString(field.column, `backend.fields[${index}].column`);

    validateJavaIdentifier(name, `backend.fields[${index}].name`);
    validateJavaType(type, `backend.fields[${index}].type`);
    validateSqlIdentifier(column, `backend.fields[${index}].column`);

    return { name, type, column };
  });
}

function normalizeKeywordColumns(keywordColumns, fields) {
  if (keywordColumns === undefined || keywordColumns === null) {
    return fields.filter((field) => field.type === "String").map((field) => field.column);
  }

  if (!Array.isArray(keywordColumns)) {
    throw new Error("Field \"backend.keywordColumns\" must be an array.");
  }

  for (const [index, column] of keywordColumns.entries()) {
    if (typeof column !== "string" || column.trim() === "") {
      throw new Error(`Field "backend.keywordColumns[${index}]" must be a non-empty string.`);
    }
    validateSqlIdentifier(column, `backend.keywordColumns[${index}]`);
  }

  return keywordColumns;
}

function controllerTemplate(context) {
  const { basePackage, className, variableName, packagePrefix, apiBasePath } = context;

  return `package ${packagePrefix}.api;

import ${basePackage}.common.list.ListResponse;
import ${packagePrefix}.dto.${className}Item;
import ${packagePrefix}.dto.${className}ListRequest;
import ${packagePrefix}.service.${className}Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${apiBasePath}")
public class ${className}Controller {

    private final ${className}Service ${variableName}Service;

    public ${className}Controller(${className}Service ${variableName}Service) {
        this.${variableName}Service = ${variableName}Service;
    }

    @PostMapping("/search")
    ListResponse<${className}Item> search(@RequestBody ${className}ListRequest request) {
        return ${variableName}Service.search(request);
    }
}
`;
}

function serviceTemplate(context) {
  const { basePackage, className, variableName, packagePrefix } = context;

  return `package ${packagePrefix}.service;

import ${basePackage}.common.list.ListResponse;
import ${basePackage}.common.list.PageSupport;
import ${packagePrefix}.dto.${className}Item;
import ${packagePrefix}.dto.${className}ListRequest;
import ${packagePrefix}.repository.${className}Repository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ${className}Service {

    private final ${className}Repository ${variableName}Repository;

    public ${className}Service(${className}Repository ${variableName}Repository) {
        this.${variableName}Repository = ${variableName}Repository;
    }

    public ListResponse<${className}Item> search(${className}ListRequest request) {
        int pageNo = PageSupport.normalizePageNo(request.pageNo());
        int pageSize = PageSupport.normalizePageSize(request.pageSize());
        int offset = PageSupport.offset(pageNo, pageSize);

        long totalCount = ${variableName}Repository.count(request.filters());
        List<${className}Item> rows = ${variableName}Repository.search(request.filters(), request.sort(), offset, pageSize);

        return new ListResponse<>(rows, totalCount, pageNo, pageSize);
    }
}
`;
}

function repositoryTemplate(context) {
  const { basePackage, className, packagePrefix, tableName, idColumn, fields, keywordColumns } = context;
  const selectColumns = fields.map((field) => `                    ${field.column}`).join(",\n");
  const keywordExpression = keywordColumns
    .map((column) => `${column} LIKE :keyword`)
    .join("\n                    OR ");
  const keywordCondition = keywordColumns.length === 0
    ? ""
    : `\n        if (filters != null && StringUtils.hasText(filters.keyword())) {\n            conditions.add("""\n                (\n                    ${keywordExpression}\n                )\n                """);\n            params.put("keyword", "%" + filters.keyword().trim() + "%");\n        }\n`;
  const orderCases = fields
    .map((field) => `            case "${field.name}" -> "${field.column}";`)
    .join("\n");

  return `package ${packagePrefix}.repository;

import ${basePackage}.common.list.ListSortRequest;
import ${basePackage}.common.persistence.QueryParts;
import ${packagePrefix}.dto.${className}Item;
import ${packagePrefix}.dto.${className}SearchFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class ${className}Repository {

    private final JdbcClient jdbcClient;

    public ${className}Repository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public long count(${className}SearchFilter filters) {
        QueryParts queryParts = buildWhere(filters);
        Long totalCount = jdbcClient.sql("SELECT COUNT(*) FROM ${tableName} " + queryParts.where())
            .params(queryParts.params())
            .query(Long.class)
            .single();

        return totalCount == null ? 0 : totalCount;
    }

    public List<${className}Item> search(${className}SearchFilter filters, List<ListSortRequest> sort, int offset, int pageSize) {
        QueryParts queryParts = buildWhere(filters);
        String orderBy = buildOrderBy(sort);

        return jdbcClient.sql("""
                SELECT
${selectColumns}
                FROM ${tableName}
                """ + queryParts.where() + orderBy + """
                OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY
                """)
            .params(queryParts.params())
            .param("offset", offset)
            .param("pageSize", pageSize)
            .query(${className}Item.class)
            .list();
    }

    private QueryParts buildWhere(${className}SearchFilter filters) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
${keywordCondition}
        if (conditions.isEmpty()) {
            return new QueryParts("", params);
        }

        return new QueryParts("WHERE " + String.join(" AND ", conditions) + " ", params);
    }

    private String buildOrderBy(List<ListSortRequest> sort) {
        if (sort == null || sort.isEmpty()) {
            return "ORDER BY ${idColumn} ";
        }

        List<String> orderItems = sort.stream()
            .map(this::toOrderItem)
            .filter(StringUtils::hasText)
            .toList();

        if (orderItems.isEmpty()) {
            return "ORDER BY ${idColumn} ";
        }

        return "ORDER BY " + String.join(", ", orderItems) + " ";
    }

    private String toOrderItem(ListSortRequest sort) {
        if (sort == null || !StringUtils.hasText(sort.field())) {
            return "";
        }

        String column = switch (sort.field()) {
${orderCases}
            default -> "";
        };

        if (!StringUtils.hasText(column)) {
            return "";
        }

        String requestedDirection = sort.direction() == null ? "" : sort.direction().toLowerCase(Locale.ROOT);
        String direction = "desc".equals(requestedDirection) ? "DESC" : "ASC";
        return column + " " + direction;
    }
}
`;
}

function itemTemplate(context) {
  const { className, packagePrefix, fields } = context;
  const imports = importsForFields(fields);
  const fieldLines = fields
    .map((field, index) => {
      const suffix = index === fields.length - 1 ? "" : ",";
      return `    ${field.type} ${field.name}${suffix}`;
    })
    .join("\n");

  return `package ${packagePrefix}.dto;
${imports}
public record ${className}Item(
${fieldLines}
) {
}
`;
}

function listRequestTemplate(context) {
  const { basePackage, className, packagePrefix } = context;

  return `package ${packagePrefix}.dto;

import ${basePackage}.common.list.ListSortRequest;
import java.util.List;

public record ${className}ListRequest(
    Integer pageNo,
    Integer pageSize,
    List<ListSortRequest> sort,
    ${className}SearchFilter filters
) {
}
`;
}

function searchFilterTemplate(context) {
  const { className, packagePrefix, keywordColumns } = context;
  const fields = keywordColumns.length === 0 ? "" : "\n    String keyword\n";

  return `package ${packagePrefix}.dto;

public record ${className}SearchFilter(${fields}) {
}
`;
}

function importsForFields(fields) {
  const imports = [...new Set(fields.map((field) => JAVA_TYPE_IMPORTS.get(field.type)).filter(Boolean))].sort();

  if (imports.length === 0) {
    return "\n";
  }

  return `\n${imports.map((value) => `import ${value};`).join("\n")}\n\n`;
}

function toApiBasePath(apiPath) {
  if (!apiPath.startsWith("/")) {
    throw new Error("Field \"apiPath\" must start with '/'.");
  }

  return apiPath.endsWith("/search") ? apiPath.slice(0, -"/search".length) : apiPath;
}

function toPackageName(value) {
  return value.replaceAll("-", "");
}

function toCamelCase(value) {
  return `${value.charAt(0).toLowerCase()}${value.slice(1)}`;
}

function requiredString(value, fieldName) {
  if (typeof value !== "string" || value.trim() === "") {
    throw new Error(`Field "${fieldName}" is required.`);
  }

  return value.trim();
}

function validateJavaPackage(value, fieldName) {
  for (const segment of value.split(".")) {
    validateJavaPackageSegment(segment, fieldName);
  }
}

function validateJavaPackageSegment(value, fieldName) {
  if (!/^[a-z][a-z0-9]*$/.test(value)) {
    throw new Error(`Field "${fieldName}" must be a lowercase Java package segment.`);
  }
}

function validateJavaIdentifier(value, fieldName) {
  if (!/^[A-Za-z_$][A-Za-z0-9_$]*$/.test(value)) {
    throw new Error(`Field "${fieldName}" must be a Java identifier.`);
  }
}

function validateJavaType(value, fieldName) {
  if (!/^[A-Z][A-Za-z0-9]*(<[^<>]+>)?$/.test(value)) {
    throw new Error(`Field "${fieldName}" must be a simple Java type.`);
  }
}

function validateSqlIdentifier(value, fieldName) {
  if (!/^[a-z][a-z0-9_]*$/.test(value)) {
    throw new Error(`Field "${fieldName}" must be a lowercase SQL identifier.`);
  }
}

function relative(backendRoot, filePath) {
  return path.relative(backendRoot, filePath).replaceAll("\\", "/");
}
