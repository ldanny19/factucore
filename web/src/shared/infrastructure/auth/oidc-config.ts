import type { UserManagerSettings } from "oidc-client-ts";
import { getRuntimeConfig } from "../../../config/runtime-config";

const runtimeConfig = getRuntimeConfig();

export const oidcConfig: UserManagerSettings = {
  authority: runtimeConfig.OIDC_AUTHORITY,
  client_id: runtimeConfig.OIDC_CLIENT_ID,
  redirect_uri: runtimeConfig.OIDC_REDIRECT_URI,
  post_logout_redirect_uri: runtimeConfig.OIDC_POST_LOGOUT_REDIRECT_URI,
  response_type: "code",
  scope: "openid profile email",
  automaticSilentRenew: true,
  accessTokenExpiringNotificationTimeInSeconds:
    runtimeConfig.SESSION_TOKEN_WARNING_MINUTES * 60,
};
