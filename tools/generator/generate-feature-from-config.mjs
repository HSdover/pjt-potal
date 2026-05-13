import { existsSync, readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import path from "node:path";
import process from "node:process";
import { generateBackend } from "./backend-generator.mjs";
import { generatePage } from "./page-generator.mjs";

const generatorRoot = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(generatorRoot, "..", "..");
const frontendRoot = path.join(repoRoot, "frontend");
const backendRoot = path.join(repoRoot, "backend");
const args = parseArgs(process.argv.slice(2));
const configPath = resolveConfigPath(args.configPath);

try {
  const config = readJson(configPath, "generator config");
  const specRef = requiredString(config.spec, "spec");
  const specPath = resolveSpecPath(specRef, configPath);
  const spec = readJson(specPath, "page spec");
  const dryRun = args.dryRun || config.dryRun === true;
  const hasBackend = spec.backend !== undefined && spec.backend !== null;

  if (args.backendOnly && !hasBackend) {
    throw new Error("backend 섹션이 없어 백엔드 파일을 생성할 수 없습니다.");
  }

  const tasks = [];

  if (!args.backendOnly) {
    tasks.push({
      label: "frontend",
      run: (taskDryRun) => generatePage(buildPageOptions(spec, config, args, taskDryRun), frontendRoot),
      print: printFrontendResult,
    });
  }

  if (!args.frontendOnly && hasBackend) {
    tasks.push({
      label: "backend",
      run: (taskDryRun) => generateBackend(buildBackendOptions(spec, config, args, taskDryRun), backendRoot),
      print: printBackendResult,
    });
  }

  if (tasks.length === 0) {
    throw new Error("생성할 대상이 없습니다.");
  }

  console.log(`config ${relative(configPath)}`);
  console.log(`spec ${relative(specPath)}`);
  console.log("");

  const checkedResults = tasks.map((task) => ({
    task,
    result: task.run(true),
  }));

  if (dryRun) {
    for (const checked of checkedResults) {
      checked.task.print(checked.result);
    }
    printNextSteps(checkedResults);
    process.exit(0);
  }

  const createdResults = tasks.map((task) => ({
    task,
    result: task.run(false),
  }));

  for (const created of createdResults) {
    created.task.print(created.result);
  }
  printNextSteps(createdResults);
} catch (error) {
  fail(error instanceof Error ? error.message : String(error));
}

function buildPageOptions(spec, config, args, dryRun) {
  return {
    pageName: requiredString(spec.feature, "feature"),
    pageType: optionalString(spec.type, "type") ?? "search-grid",
    title: optionalString(spec.title, "title"),
    description: optionalString(spec.description, "description"),
    authCode: optionalString(spec.auth, "auth"),
    apiPath: optionalString(spec.apiPath, "apiPath"),
    force: args.force || config.force === true || spec.force === true,
    dryRun,
  };
}

function buildBackendOptions(spec, config, args, dryRun) {
  const backend = requiredObject(spec.backend, "backend");

  return {
    feature: requiredString(spec.feature, "feature"),
    apiPath: requiredString(spec.apiPath, "apiPath"),
    basePackage: optionalString(backend.basePackage, "backend.basePackage"),
    packageName: optionalString(backend.packageName, "backend.packageName"),
    className: optionalString(backend.className, "backend.className"),
    tableName: backend.tableName,
    idColumn: backend.idColumn,
    fields: backend.fields,
    keywordColumns: backend.keywordColumns,
    force: args.force || config.force === true || spec.force === true || backend.force === true,
    dryRun,
  };
}

function parseArgs(values) {
  const options = {
    configPath: "",
    dryRun: false,
    force: false,
    frontendOnly: false,
    backendOnly: false,
  };

  for (let index = 0; index < values.length; index += 1) {
    const value = values[index];

    if (value === "--config") {
      const nextValue = values[index + 1];
      if (!nextValue || nextValue.startsWith("--")) {
        fail("Missing value for --config.");
      }
      options.configPath = nextValue;
      index += 1;
      continue;
    }

    if (value === "--dry-run") {
      options.dryRun = true;
      continue;
    }

    if (value === "--force") {
      options.force = true;
      continue;
    }

    if (value === "--frontend-only") {
      options.frontendOnly = true;
      continue;
    }

    if (value === "--backend-only") {
      options.backendOnly = true;
      continue;
    }

    fail(`Unknown option: ${value}`);
  }

  if (options.frontendOnly && options.backendOnly) {
    fail("--frontend-only와 --backend-only는 함께 사용할 수 없습니다.");
  }

  return options;
}

function resolveConfigPath(configPath) {
  if (!configPath) {
    return path.join(generatorRoot, "generator.config.json");
  }

  const resolvedPath = path.isAbsolute(configPath)
    ? configPath
    : path.resolve(repoRoot, configPath);

  if (!existsSync(resolvedPath)) {
    throw new Error(`Generator config not found: ${configPath}`);
  }

  return resolvedPath;
}

function resolveSpecPath(specRef, currentConfigPath) {
  const configDir = path.dirname(currentConfigPath);
  const directPath = path.isAbsolute(specRef)
    ? specRef
    : path.resolve(configDir, specRef);

  if (existsSync(directPath)) {
    return directPath;
  }

  const fromRepoRoot = path.resolve(repoRoot, specRef);
  if (existsSync(fromRepoRoot)) {
    return fromRepoRoot;
  }

  const fileName = specRef.endsWith(".json") ? specRef : `${specRef}.json`;
  const namedPath = path.join(generatorRoot, "pages", fileName);

  if (existsSync(namedPath)) {
    return namedPath;
  }

  throw new Error(`Page spec not found: ${specRef}`);
}

function readJson(filePath, label) {
  try {
    return JSON.parse(readFileSync(filePath, "utf8"));
  } catch (error) {
    throw new Error(`Invalid ${label} JSON: ${relative(filePath)}. ${error instanceof Error ? error.message : String(error)}`);
  }
}

function requiredObject(value, fieldName) {
  if (!value || typeof value !== "object" || Array.isArray(value)) {
    throw new Error(`Field "${fieldName}" is required.`);
  }

  return value;
}

function requiredString(value, fieldName) {
  const result = optionalString(value, fieldName);
  if (!result) {
    throw new Error(`Field "${fieldName}" is required.`);
  }

  return result;
}

function optionalString(value, fieldName) {
  if (value === undefined || value === null || value === "") {
    return undefined;
  }

  if (typeof value !== "string") {
    throw new Error(`Field "${fieldName}" must be a string.`);
  }

  return value;
}

function printFrontendResult(result) {
  printFiles("프론트엔드", result);
}

function printBackendResult(result) {
  printFiles("백엔드", result);
}

function printFiles(label, result) {
  const action = result.dryRun ? "생성 예정" : "생성 완료";

  console.log(`[${label}]`);
  for (const file of result.files) {
    console.log(`${action} ${file}`);
  }
  console.log("");
}

function printNextSteps(results) {
  const frontend = results.find((item) => item.task.label === "frontend")?.result;
  const backend = results.find((item) => item.task.label === "backend")?.result;

  console.log("다음 작업:");
  if (frontend) {
    console.log(`- ${frontend.pageComponentName} 라우트를 등록하고 화면 필드를 업무 기준으로 조정합니다.`);
  }
  if (backend) {
    console.log(`- ${backend.packageName} 패키지를 검토하고 ${backend.apiBasePath}용 schema/data/test를 추가합니다.`);
  }
}

function relative(filePath) {
  return path.relative(repoRoot, filePath).replaceAll("\\", "/");
}

function fail(message) {
  console.error(message);
  process.exit(1);
}
