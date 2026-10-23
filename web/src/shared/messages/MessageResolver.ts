import { MessageCodes, type MessageCode } from "./MessageCodes";

declare global {
  interface Window {
    FACTUCORE_MESSAGES?: Partial<Record<MessageCode, string>>;
  }
}

export function resolveMessage(code: MessageCode, parameters: Record<string, string | number> = {}): string {
  const template = window.FACTUCORE_MESSAGES?.[code]?.trim();
  if (!template) {
    console.error(`No se encontró la descripción externa para el código de mensaje: ${code}`);
    const fallback = window.FACTUCORE_MESSAGES?.[MessageCodes.COMMON_MESSAGE_NOT_CONFIGURED]?.trim();
    return fallback || code;
  }
  return template.replace(/\{([^}]+)\}/g, (placeholder, key: string) =>
    Object.prototype.hasOwnProperty.call(parameters, key) ? String(parameters[key]) : placeholder,
  );
}
