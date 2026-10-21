import { getRuntimeConfig } from "../../config/runtime-config";

export function resolveMessage(code: string, parameters: Record<string, string | number> = {}): string {
  const messages = getRuntimeConfig().MESSAGES;
  const template = messages[code] ?? messages["common.operationFailed"] ?? code;
  return template.replace(/\{([^}]+)\}/g, (placeholder, key: string) =>
    Object.prototype.hasOwnProperty.call(parameters, key) ? String(parameters[key]) : placeholder,
  );
}
