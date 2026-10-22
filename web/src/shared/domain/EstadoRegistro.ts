/** Valores canónicos de ESTADO_REGISTRO definidos por el backend FactuCore. */
export const EstadoRegistro = {
  ACTIVO: "A",
  INACTIVO: "I",
  ELIMINADO: "X",
  REEMPLAZADO: "R",
} as const;

export type EstadoRegistro = (typeof EstadoRegistro)[keyof typeof EstadoRegistro];
