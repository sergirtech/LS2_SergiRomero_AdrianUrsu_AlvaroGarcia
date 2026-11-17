export const getEnv = () => {
  const { VITE_API_DOMAIN, VITE_MEDIA_DOMAIN, ...otherViteConfig } = import.meta.env;

  return {
    API_BASE_URL: `${VITE_API_DOMAIN}/api`,
    MEDIA_BASE_URL: `${VITE_MEDIA_DOMAIN}/media`,
    __vite__: otherViteConfig,
  };
};

export const GOOGLE_OAUTH_URL = `${getEnv()
  .API_BASE_URL.replace(/\/api\/?$/, '')
  .replace(/\/$/, '')}/oauth2/authorization/google`;
