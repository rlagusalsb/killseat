const A = "accessToken";
const R = "refreshToken";

export const token = {
  getAccess: () => localStorage.getItem(A),
  setAccess: (v) => localStorage.setItem(A, v),
  getRefresh: () => localStorage.getItem(R),
  setRefresh: (v) => localStorage.setItem(R, v),
  clear: () => {
    localStorage.removeItem(A);
    localStorage.removeItem(R);
  },
};
