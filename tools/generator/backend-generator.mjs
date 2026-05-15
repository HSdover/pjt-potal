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
  const idField = findIdField(fields, idColumn);
  const keywordFields = normalizeKeywordFields(options, fields);
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
    entityVariableName: toCamelCase(className),
    serviceVariableName: `${toCamelCase(className)}Service`,
    repositoryVariableName: `${toCamelCase(className)}Repository`,
    tableName,
    apiBasePath,
    fields,
    idField,
    keywordFields,
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
      path: path.join(featureRoot, "domain", `${className}.java`),
      content: entityTemplate(context),
    },
    {
      path: path.join(featureRoot, "repository", `${className}Repository.java`),
      content: repositoryTemplate(context),
    },
    {
      path: path.join(featureRoot, "repository", `${className}QueryRepository.java`),
      content: queryRepositoryTemplate(context),
    },
    {
      path: path.join(featureRoot, "repository", `${className}QueryRepositoryImpl.java`),
      content: queryRepositoryImplTemplate(context),
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

function findIdField(fields, idColumn) {
  const idField = fields.find((field) => field.column === idColumn);
  if (!idField) {
    throw new Error(`Field "backend.idColumn" must match one backend.fields[].column value: ${idColumn}`);
  }

  return idField;
}

function normalizeKeywordFields(options, fields) {
  const configured = options.keywordFields ?? options.keywordColumns;
  const values = configured === undefined || configured === null
    ? fields.filter((field) => field.type === "String").map((field) => field.name)
    : configured;

  if (!Array.isArray(values)) {
    throw new Error("Field \"backend.keywordFields\" or \"backend.keywordColumns\" must be an array.");
  }

  const result = [];
  const seen = new Set();

  for (const [index, value] of values.entries()) {
    if (typeof value !== "string" || value.trim() === "") {
      throw new Error(`Keyword field at index ${index} must be a non-empty string.`);
    }

    const field = fields.find((candidate) => candidate.name === value || candidate.column === value);
    if (!field) {
      throw new Error(`Keyword field "${value}" must match a backend.fields[].name or backend.fields[].column value.`);
    }

    if (field.type !== "String") {
      throw new Error(`Keyword field "${value}" must reference a String field.`);
    }

    if (!seen.has(field.name)) {
      seen.add(field.name);
      result.push(field);
    }
  }

  return result;
}

function controllerTemplate(context) {
  const { basePackage, className, serviceVariableName, packagePrefix, apiBasePath } = context;

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

    private final ${className}Service ${serviceVariableName};

    public ${className}Controller(${className}Service ${serviceVariableName}) {
        this.${serviceVariableName} = ${serviceVariableName};
    }

    @PostMapping("/search")
    ListResponse<${className}Item> search(@RequestBody ${className}ListRequest request) {
        return ${serviceVariableName}.search(request);
    }
}
`;
}

function serviceTemplate(context) {
  const { basePackage, className, packagePrefix, repositoryVariableName, fields } = context;
  const sortCases = fields
    .map((field) => `            case "${field.name}" -> "${field.name}";`)
    .join("\n");

  return `package ${packagePrefix}.service;

import ${basePackage}.common.list.ListResponse;
import ${basePackage}.common.list.ListSortRequest;
import ${basePackage}.common.list.PageSupport;
import ${packagePrefix}.domain.${className};
import ${packagePrefix}.dto.${className}Item;
import ${packagePrefix}.dto.${className}ListRequest;
import ${packagePrefix}.repository.${className}Repository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ${className}Service {

    private final ${className}Repository ${repositoryVariableName};

    public ${className}Service(${className}Repository ${repositoryVariableName}) {
        this.${repositoryVariableName} = ${repositoryVariableName};
    }

    @Transactional(readOnly = true)
    public ListResponse<${className}Item> search(${className}ListRequest request) {
        int pageNo = PageSupport.normalizePageNo(request.pageNo());
        int pageSize = PageSupport.normalizePageSize(request.pageSize());
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, toSort(request.sort()));

        Page<${className}> page = ${repositoryVariableName}.search(request.filters(), pageable);
        List<${className}Item> rows = page.getContent().stream()
            .map(${className}Item::from)
            .toList();

        return new ListResponse<>(rows, page.getTotalElements(), pageNo, pageSize);
    }

    private Sort toSort(List<ListSortRequest> sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by(Sort.Order.asc("${context.idField.name}"));
        }

        List<Sort.Order> orders = sort.stream()
            .map(this::toOrder)
            .filter(order -> order != null)
            .toList();

        if (orders.isEmpty()) {
            return Sort.by(Sort.Order.asc("${context.idField.name}"));
        }

        return Sort.by(orders);
    }

    private Sort.Order toOrder(ListSortRequest sort) {
        if (sort == null || !StringUtils.hasText(sort.field())) {
            return null;
        }

        String property = switch (sort.field()) {
${sortCases}
            default -> "";
        };

        if (!StringUtils.hasText(property)) {
            return null;
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sort.direction()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return new Sort.Order(direction, property);
    }
}
`;
}

function entityTemplate(context) {
  const { className, packagePrefix, tableName, fields, idField } = context;
  const imports = importsForFields(fields);
  const fieldLines = fields.map((field) => entityFieldTemplate(field, idField)).join("\n\n");
  const getterLines = fields.map((field) => getterTemplate(field)).join("\n\n");

  return `package ${packagePrefix}.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
${imports}
@Entity
@Table(name = "${tableName}")
public class ${className} {

${fieldLines}

    protected ${className}() {
    }

${getterLines}
}
`;
}

function entityFieldTemplate(field, idField) {
  const idAnnotations = field.name === idField.name
    ? "    @Id\n    @GeneratedValue(strategy = GenerationType.IDENTITY)\n"
    : "";

  return `${idAnnotations}    @Column(name = "${field.column}")
    private ${field.type} ${field.name};`;
}

function getterTemplate(field) {
  return `    public ${field.type} get${toPascalCase(field.name)}() {
        return ${field.name};
    }`;
}

function repositoryTemplate(context) {
  const { className, packagePrefix, idField } = context;

  return `package ${packagePrefix}.repository;

import ${packagePrefix}.domain.${className};
import org.springframework.data.jpa.repository.JpaRepository;

public interface ${className}Repository extends JpaRepository<${className}, ${idField.type}>, ${className}QueryRepository {
}
`;
}

function queryRepositoryTemplate(context) {
  const { className, packagePrefix } = context;

  return `package ${packagePrefix}.repository;

import ${packagePrefix}.domain.${className};
import ${packagePrefix}.dto.${className}SearchFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ${className}QueryRepository {

    Page<${className}> search(${className}SearchFilter filters, Pageable pageable);
}
`;
}

function queryRepositoryImplTemplate(context) {
  const { className, packagePrefix, entityVariableName, fields, keywordFields } = context;
  const orderCases = fields
    .map((field) => `            case "${field.name}" -> Optional.of(new OrderSpecifier<>(direction, ${entityVariableName}.${field.name}));`)
    .join("\n");
  const containsKeywordMethod = keywordPredicateTemplate(context);

  return `package ${packagePrefix}.repository;

import static ${packagePrefix}.domain.Q${className}.${entityVariableName};

import ${packagePrefix}.domain.${className};
import ${packagePrefix}.dto.${className}SearchFilter;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public class ${className}QueryRepositoryImpl implements ${className}QueryRepository {

    private final JPAQueryFactory queryFactory;

    public ${className}QueryRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Page<${className}> search(${className}SearchFilter filters, Pageable pageable) {
        Predicate keywordCondition = containsKeyword(filters);

        JPAQuery<${className}> rowQuery = queryFactory
            .selectFrom(${entityVariableName})
            .orderBy(toOrderSpecifiers(pageable.getSort()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        JPAQuery<Long> countQuery = queryFactory
            .select(${entityVariableName}.count())
            .from(${entityVariableName});

        if (keywordCondition != null) {
            rowQuery.where(keywordCondition);
            countQuery.where(keywordCondition);
        }

        List<${className}> rows = rowQuery.fetch();
        Long totalCount = countQuery.fetchOne();

        return new PageImpl<>(rows, pageable, totalCount == null ? 0 : totalCount);
    }

${containsKeywordMethod}

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (sort != null) {
            for (Sort.Order order : sort) {
                toOrderSpecifier(order).ifPresent(orderSpecifiers::add);
            }
        }

        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers.add(${entityVariableName}.${context.idField.name}.asc());
        }

        return orderSpecifiers.toArray(OrderSpecifier<?>[]::new);
    }

    private Optional<OrderSpecifier<?>> toOrderSpecifier(Sort.Order sort) {
        Order direction = sort.isDescending() ? Order.DESC : Order.ASC;
        return switch (sort.getProperty()) {
${orderCases}
            default -> Optional.empty();
        };
    }
}
`;
}

function keywordPredicateTemplate(context) {
  const { className, entityVariableName, keywordFields } = context;

  if (keywordFields.length === 0) {
    return `    private Predicate containsKeyword(${className}SearchFilter filters) {
        return null;
    }`;
  }

  const [firstField, ...remainingFields] = keywordFields;
  const expression = [
    `${entityVariableName}.${firstField.name}.containsIgnoreCase(keyword)`,
    ...remainingFields.map((field) => `            .or(${entityVariableName}.${field.name}.containsIgnoreCase(keyword))`),
  ].join("\n");

  return `    private Predicate containsKeyword(${className}SearchFilter filters) {
        if (filters == null || !StringUtils.hasText(filters.keyword())) {
            return null;
        }

        String keyword = filters.keyword().trim();
        return ${expression};
    }`;
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
  const fromArgs = fields
    .map((field) => `            source.get${toPascalCase(field.name)}()`)
    .join(",\n");

  return `package ${packagePrefix}.dto;

import ${packagePrefix}.domain.${className};
${imports}
public record ${className}Item(
${fieldLines}
) {
    public static ${className}Item from(${className} source) {
        return new ${className}Item(
${fromArgs}
        );
    }
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
  const { className, packagePrefix, keywordFields } = context;
  const fields = keywordFields.length === 0 ? "" : "\n    String keyword\n";

  return `package ${packagePrefix}.dto;

public record ${className}SearchFilter(${fields}) {
}
`;
}

function importsForFields(fields) {
  const imports = [...new Set(fields.map((field) => JAVA_TYPE_IMPORTS.get(field.type)).filter(Boolean))].sort();

  if (imports.length === 0) {
    return "";
  }

  return `${imports.map((value) => `import ${value};`).join("\n")}\n\n`;
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
