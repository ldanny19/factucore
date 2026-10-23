export interface UsuarioKeycloak {
  id: string;
  username: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  enabled: boolean;
  emailVerified?: boolean;
}

export interface RolKeycloak {
  id: string;
  name: string;
  description?: string;
}

export const PERMISOS_FACTUCORE = [
  {
    name: "GENERATE_COMPROBANTE",
    label: "Generar comprobantes",
    description: "Permite iniciar la emisión de comprobantes electrónicos.",
  },
  {
    name: "REPROCESS_COMPROBANTE",
    label: "Reprocesar comprobantes",
    description: "Permite solicitar el reproceso de comprobantes con errores.",
  },
] as const;

export type PermisoFactuCore = (typeof PERMISOS_FACTUCORE)[number]["name"];
