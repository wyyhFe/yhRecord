import { defineStore } from 'pinia'
import { fetchReminderSettings, type ReminderSetting } from '@/api/reminder'

export const useReminderStore = defineStore('reminder', {
  state: () => ({
    settings: null as ReminderSetting | null
  }),
  actions: {
    async load() {
      this.settings = await fetchReminderSettings()
    }
  }
})
