export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken())
  const logged = computed(
    () => !!token.value && token.value.expires > Date.now(),
  )
  const username = computed(() => token.value?.username ?? null)

  function login(d: {
    accessToken: string
    refreshToken: string
    expiresIn: number
    username?: string
  }) {
    setToken(d)
    token.value = getToken()
  }
  function logout() {
    removeToken()
    token.value = null
    navigateTo('/')
  }

  return { token, logged, username, login, logout }
})
