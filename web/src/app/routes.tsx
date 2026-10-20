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
        <Route path="comprobantes" element={<ModulePlaceholder title="Comprobantes" />} />
        <Route path="configuracion" element={<ModulePlaceholder title="Configuración" />} />
        <Route path="accesos" element={<ModulePlaceholder title="Administración de accesos" />} />
      </Route>
      <Route path="/empresas" element={<Navigate to="/app/empresas" replace />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
