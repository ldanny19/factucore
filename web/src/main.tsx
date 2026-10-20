import React from "react";
import ReactDOM from "react-dom/client";
import AppProviders from "./app/AppProviders";
import AppRoutes from "./app/routes";
import ConfigurationRequired from "./ConfigurationRequired";
import { getRuntimeConfig } from "./config/runtime-config";
import "./styles.css";
const root = ReactDOM.createRoot(document.getElementById("root")!);
try { getRuntimeConfig(); root.render(<React.StrictMode><AppProviders><AppRoutes /></AppProviders></React.StrictMode>); } catch (error) { const message = error instanceof Error ? error.message : "Error desconocido de configuración."; root.render(<React.StrictMode><ConfigurationRequired message={message} /></React.StrictMode>); }
