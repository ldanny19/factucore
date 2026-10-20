import { Navigate, Route, Routes } from "react-router-dom";
import App from "./App";
import AppLayout from "./layout/AppLayout";
import DashboardPage from "../features/dashboard/presentation/DashboardPage";
import LoginPage from "../features/login/presentation/LoginPage";
import RecuperarContrasenaPage from "../features/login/presentation/RecuperarContrasenaPage";
import EmpresasPage from "../features/empresas/presentation/EmpresasPage";

function ModulePlaceholder({ title }: { title: string }) {
  return (
    <div className="module-placeholder">
      <h1>{title}</h1>
      <p>Esta opción está preparada en el menú. Su funcionalidad se implementará en una tarea posterior.</p>
    </div>
  );
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<App />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/recuperar-contrasena" element={<RecuperarContrasenaPage />} />
      <Route path="/app" element={<AppLayout />}>
        <Route index element={<DashboardPage />} />

        <Route path="empresas" element={<EmpresasPage />} />
        <Route path="establecimientos" element={<ModulePlaceholder title="Establecimientos" />} />
        <Route path="puntos-emision" element={<ModulePlaceholder title="Puntos de emisión" />} />
        <Route path="secuenciales" element={<ModulePlaceholder title="Secuenciales" />} />
        <Route path="certificados-firma" element={<ModulePlaceholder title="Certificados de firma electrónica" />} />

        <Route path="configuracion">
          <Route index element={<Navigate to="empresa" replace />} />
          <Route path="empresa" element={<ModulePlaceholder title="Configuración de empresa" />} />
        </Route>
        <Route path="tarifas-tributarias" element={<ModulePlaceholder title="Tarifas tributarias" />} />
        <Route path="catalogos-tributarios" element={<ModulePlaceholder title="Catálogos tributarios" />} />

        <Route path="definiciones-documentales" element={<ModulePlaceholder title="Definiciones documentales y XSD" />} />
        <Route path="mapeos-xsd" element={<ModulePlaceholder title="Mapeos XSD" />} />

        <Route path="auditoria">
          <Route index element={<Navigate to="historial" replace />} />
          <Route path="historial" element={<ModulePlaceholder title="Historial de acciones" />} />
          <Route path="trazabilidad" element={<ModulePlaceholder title="Trazabilidad de comprobantes y procesos" />} />
          <Route path="evidencias" element={<ModulePlaceholder title="Evidencias de procesamiento" />} />
        </Route>

        <Route path="reportes-comprobantes">
          <Route index element={<Navigate to="resumen" replace />} />
          <Route path="resumen" element={<ModulePlaceholder title="Resumen del día" />} />
          <Route path="consulta" element={<ModulePlaceholder title="Consulta de comprobantes" />} />
          <Route path="seguimiento" element={<ModulePlaceholder title="Seguimiento de estados" />} />
          <Route path="pendientes-fallidos" element={<ModulePlaceholder title="Comprobantes pendientes o fallidos" />} />
          <Route path="busqueda" element={<ModulePlaceholder title="Filtros y búsqueda de comprobantes" />} />
        </Route>

        <Route path="comprobantes" element={<Navigate to="/app/reportes-comprobantes" replace />} />

        <Route path="accesos">
          <Route index element={<Navigate to="usuarios" replace />} />
          <Route path="usuarios" element={<ModulePlaceholder title="Usuarios" />} />
          <Route path="roles" element={<ModulePlaceholder title="Roles" />} />
          <Route path="permisos" element={<ModulePlaceholder title="Permisos" />} />
          <Route path="asignacion-roles" element={<ModulePlaceholder title="Asignación de roles a usuarios" />} />
        </Route>
      </Route>
      <Route path="/empresas" element={<Navigate to="/app/empresas" replace />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
