export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken())
  const logged = computed(() => !!token.value && token.value.expires > Date.now())

  function login(d: { accessToken: string; refreshToken: string; expiresIn: number }) { setToken(d); token.value = getToken() }
  function logout() { removeToken(); token.value = null; navigateTo('/login') }

  return { token, logged, login, logout }
})
