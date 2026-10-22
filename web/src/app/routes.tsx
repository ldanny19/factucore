import { Navigate, Route, Routes } from "react-router-dom";
import { CircularProgress } from "@mui/material";
import { useAuth } from "react-oidc-context";
import type { ReactNode } from "react";
import App from "./App";
import AppLayout from "./layout/AppLayout";
import DashboardPage from "../features/dashboard/presentation/DashboardPage";
import LoginPage from "../features/login/presentation/LoginPage";
import RecuperarContrasenaPage from "../features/login/presentation/RecuperarContrasenaPage";
import EmpresasPage from "../features/empresas/presentation/EmpresasPage";
import EstablecimientosPage from "../features/establecimientos/presentation/EstablecimientosPage";
import PuntosEmisionPage from "../features/puntos-emision/presentation/PuntosEmisionPage";
import RequireAccess from "./security/RequireAccess";
import { APP_ROLES, getAllowedRolesForPath } from "../shared/security/authorization";

function AuthCallback() {
  const auth = useAuth();
  if (auth.isLoading) return <CircularProgress />;
  return <Navigate to={auth.isAuthenticated ? "/app" : "/login"} replace />;
}

function ModulePlaceholder({ title }: { title: string }) {
  return (
    <div className="module-placeholder">
      <h1>{title}</h1>
      <p>Esta opción está preparada en el menú. Su funcionalidad se implementará en una tarea posterior.</p>
    </div>
  );
}

function guarded(path: string, element: ReactNode) {
  return <RequireAccess allowedRoles={getAllowedRolesForPath(path)}>{element}</RequireAccess>;
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<App />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/auth/callback" element={<AuthCallback />} />
      <Route path="/recuperar-contrasena" element={<RecuperarContrasenaPage />} />
      <Route path="/app" element={<RequireAccess allowedRoles={APP_ROLES}><AppLayout /></RequireAccess>}>
        <Route index element={guarded("/app", <DashboardPage />)} />
        <Route path="empresas" element={guarded("/app/empresas", <EmpresasPage />)} />
        <Route path="establecimientos" element={guarded("/app/establecimientos", <EstablecimientosPage />)} />
        <Route path="puntos-emision" element={guarded("/app/puntos-emision", <PuntosEmisionPage />)} />
        <Route path="secuenciales" element={guarded("/app/secuenciales", <ModulePlaceholder title="Secuenciales" />)} />
        <Route path="certificados-firma" element={guarded("/app/certificados-firma", <ModulePlaceholder title="Certificados de firma electrónica" />)} />
        <Route path="configuracion">
          <Route index element={<Navigate to="empresa" replace />} />
          <Route path="empresa" element={guarded("/app/configuracion/empresa", <ModulePlaceholder title="Configuración de empresa" />)} />
        </Route>
        <Route path="tarifas-tributarias" element={guarded("/app/tarifas-tributarias", <ModulePlaceholder title="Tarifas tributarias" />)} />
        <Route path="catalogos-tributarios" element={guarded("/app/catalogos-tributarios", <ModulePlaceholder title="Catálogos tributarios" />)} />
        <Route path="definiciones-documentales" element={guarded("/app/definiciones-documentales", <ModulePlaceholder title="Definiciones documentales y XSD" />)} />
        <Route path="mapeos-xsd" element={guarded("/app/mapeos-xsd", <ModulePlaceholder title="Mapeos XSD" />)} />
        <Route path="auditoria">
          <Route index element={<Navigate to="historial" replace />} />
          <Route path="historial" element={guarded("/app/auditoria/historial", <ModulePlaceholder title="Historial de acciones" />)} />
          <Route path="trazabilidad" element={guarded("/app/auditoria/trazabilidad", <ModulePlaceholder title="Trazabilidad de comprobantes y procesos" />)} />
          <Route path="evidencias" element={guarded("/app/auditoria/evidencias", <ModulePlaceholder title="Evidencias de procesamiento" />)} />
        </Route>
        <Route path="reportes-comprobantes">
          <Route index element={<Navigate to="resumen" replace />} />
          <Route path="resumen" element={guarded("/app/reportes-comprobantes/resumen", <ModulePlaceholder title="Resumen del día" />)} />
          <Route path="consulta" element={guarded("/app/reportes-comprobantes/consulta", <ModulePlaceholder title="Consulta de comprobantes" />)} />
          <Route path="seguimiento" element={guarded("/app/reportes-comprobantes/seguimiento", <ModulePlaceholder title="Seguimiento de estados" />)} />
          <Route path="pendientes-fallidos" element={guarded("/app/reportes-comprobantes/pendientes-fallidos", <ModulePlaceholder title="Comprobantes pendientes o fallidos" />)} />
          <Route path="busqueda" element={guarded("/app/reportes-comprobantes/busqueda", <ModulePlaceholder title="Filtros y búsqueda de comprobantes" />)} />
        </Route>
        <Route path="comprobantes" element={<Navigate to="/app/reportes-comprobantes" replace />} />
        <Route path="accesos">
          <Route index element={<Navigate to="usuarios" replace />} />
          <Route path="usuarios" element={guarded("/app/accesos/usuarios", <ModulePlaceholder title="Usuarios" />)} />
          <Route path="roles" element={guarded("/app/accesos/roles", <ModulePlaceholder title="Roles" />)} />
          <Route path="permisos" element={guarded("/app/accesos/permisos", <ModulePlaceholder title="Permisos" />)} />
          <Route path="asignacion-roles" element={guarded("/app/accesos/asignacion-roles", <ModulePlaceholder title="Asignación de roles a usuarios" />)} />
        </Route>
      </Route>
      <Route path="/empresas" element={<Navigate to="/app" replace />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
