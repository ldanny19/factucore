import { Navigate, Route, Routes } from "react-router-dom";
import App from "./App";
import LoginPage from "../features/login/presentation/LoginPage";
import EmpresasPage from "../features/empresas/presentation/EmpresasPage";
export default function AppRoutes() { return <Routes><Route path="/" element={<App />} /><Route path="/login" element={<LoginPage />} /><Route path="/empresas" element={<EmpresasPage />} /><Route path="*" element={<Navigate to="/" replace />} /></Routes>; }
