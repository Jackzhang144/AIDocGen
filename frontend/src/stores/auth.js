import { computed, reactive } from 'vue';

const TOKEN_KEY = 'aidoc_token';
const USER_KEY = 'aidoc_user';

const state = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  user: safeParse(localStorage.getItem(USER_KEY))
});

function safeParse(raw) {
  try {
    return raw ? JSON.parse(raw) : null;
  } catch (error) {
    return null;
  }
}

export function useAuthStore() {
  const setAuth = (payload) => {
    state.token = payload.token;
    state.user = {
      username: payload.username,
      email: payload.email,
      role: payload.role,
      apiQuota: payload.apiQuota
    };
    localStorage.setItem(TOKEN_KEY, state.token);
    localStorage.setItem(USER_KEY, JSON.stringify(state.user));
  };

  const logout = () => {
    state.token = '';
    state.user = null;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  };

  const isAuthenticated = computed(() => Boolean(state.token));
  const isAdmin = computed(() => state.user?.role === 'ADMIN');
  const isPremium = computed(() => state.user?.role === 'PREMIUM' || state.user?.role === 'ADMIN');

  return {
    state,
    setAuth,
    logout,
    isAuthenticated,
    isAdmin,
    isPremium
  };
}
