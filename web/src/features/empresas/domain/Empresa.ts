export interface Empresa {
  id: number;
  ruc: string;
  razonSocial: string;
  nombreComercial: string | null;
  direccionMatriz: string;
  obligadoContabilidad: "SI" | "NO";
  contribuyenteRimpe: string | null;
  estadoRegistro: "ACTIVO" | "INACTIVO" | "ELIMINADO" | string;
  usuarioCreacion?: string | null;
  usuarioModificacion?: string | null;
  fechaCreacion?: string | null;
  fechaModificacion?: string | null;
  observacion?: string | null;
}

export interface EmpresaDatos {
  ruc: string;
  razonSocial: string;
  nombreComercial: string | null;
  direccionMatriz: string;
  obligadoContabilidad: "SI" | "NO";
  contribuyenteRimpe: string | null;
}
