import api from "./client";
import { token } from "../auth/token";

let refreshing = false;
let queue = [];

const flush = (newToken) => {
  queue.forEach((cb) => cb(newToken));
  queue = [];
};

export function setupInterceptors() {
  api.interceptors.request.use((config) => {
    const at = token.getAccess();
    if (at) config.headers.Authorization = `Bearer ${at}`;
    return config;
  });

  api.interceptors.response.use(
    (res) => res,
    async (err) => {
      const original = err.config;
      if (err.response?.status !== 401 || original?._retry) throw err;
      original._retry = true;

      const rt = token.getRefresh();
      if (!rt) {
        token.clear();
        window.location.href = "/login";
        throw err;
      }

      if (refreshing) {
        return new Promise((resolve) => {
          queue.push((newAT) => {
            original.headers.Authorization = `Bearer ${newAT}`;
            resolve(api(original));
          });
        });
      }

      refreshing = true;
      try {
        const r = await api.post("/api/auth/refresh", { refreshToken: rt });
        const newAT = r.data.accessToken;
        token.setAccess(newAT);
        token.setRefresh(r.data.refreshToken);
        flush(newAT);
        original.headers.Authorization = `Bearer ${newAT}`;
        return api(original);
      } finally {
        refreshing = false;
      }
    }
  );
}
