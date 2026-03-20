import { defineStore } from 'pinia'
import { fetchUserProfile } from '@/api/user'
import type { UserProfile } from '@/types/domain'

export const useAppStore = defineStore('app', {
  state: () => ({
    profile: null as UserProfile | null,
    loading: false
  }),
  actions: {
    async loadProfile() {
      this.loading = true
      try {
        this.profile = await fetchUserProfile()
      } finally {
        this.loading = false
      }
    }
  }
})
