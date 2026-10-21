export interface AdministracionRequest<T> {
  fechaInicio: string;
  canal: string;
  idEmpresa: number | null;
  datos: T;
}

export interface AdministracionGlobalRequest<T> {
  idTransaccion: string;
  fechaInicio: string;
  canal: string;
  datos: T;
}

export interface AdministracionResponse<T> {
  idTransaccion: string | null;
  fechaInicio: string | null;
  fechaFin: string | null;
  exitoso: boolean;
  codigo: string | null;
  mensaje: string | null;
  datos: T;
}
