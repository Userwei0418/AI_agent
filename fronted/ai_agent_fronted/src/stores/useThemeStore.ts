import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    currentTheme: 'light' // 可选值：light/dark
  }),
  actions: {
    setTheme(theme: 'light' | 'dark') {
      this.currentTheme = theme
    }
  }
})
