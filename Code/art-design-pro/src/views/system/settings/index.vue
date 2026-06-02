<template>
  <div class="settings-page" style="max-width: 820px; margin: 0 auto; padding: 16px 24px 32px">
    <h1 style="font-size: 22px; font-weight: 600; margin-bottom: 8px">个性设置</h1>
    <p style="color: #888; font-size: 14px; margin-bottom: 28px">自定义系统外观和交互行为</p>

    <div style="display: flex; flex-direction: column; gap: 24px">
      <!-- 主题风格 -->
      <div class="settings-card" style="background: var(--art-card-bg-color); border: 1px solid var(--art-card-border); border-radius: 10px; padding: 24px;">
        <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--art-gray-200);">主题风格</h3>
        <ThemeSettings />
      </div>
      <!-- 系统主题色 -->
      <div
        class="settings-card"
        style="
          background: var(--art-card-bg-color);
          border: 1px solid var(--art-card-border);
          border-radius: 10px;
          padding: 24px;
        "
      >
        <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--art-gray-200);">系统主题色</h3>
        <ColorSettings />
      </div>
      <!-- 菜单布局 -->
      <div class="settings-card" style="background: var(--art-card-bg-color); border: 1px solid var(--art-card-border); border-radius: 10px; padding: 24px;">
        <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--art-gray-200);">菜单布局</h3>
        <MenuLayoutSettings />
      </div>
      <!-- 菜单风格 -->
      <div class="settings-card" style="background: var(--art-card-bg-color); border: 1px solid var(--art-card-border); border-radius: 10px; padding: 24px;">
        <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--art-gray-200);">菜单风格</h3>
        <MenuStyleSettings />
      </div>
      <!-- 盒子样式 -->
      <div class="settings-card" style="background: var(--art-card-bg-color); border: 1px solid var(--art-card-border); border-radius: 10px; padding: 24px;">
        <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--art-gray-200);">盒子样式</h3>
        <BoxStyleSettings />
      </div>
      <!-- 容器宽度 -->
      <div class="settings-card" style="background: var(--art-card-bg-color); border: 1px solid var(--art-card-border); border-radius: 10px; padding: 24px;">
        <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--art-gray-200);">容器宽度</h3>
        <ContainerSettings />
      </div>
      <!-- 基础配置 -->
      <div class="settings-card" style="background: var(--art-card-bg-color); border: 1px solid var(--art-card-border); border-radius: 10px; padding: 24px;">
        <h3 style="font-size: 16px; font-weight: 600; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--art-gray-200);">基础配置</h3>
        <BasicSettings />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import ThemeSettings from '@/components/core/layouts/art-settings-panel/widget/ThemeSettings.vue'
  import MenuLayoutSettings from '@/components/core/layouts/art-settings-panel/widget/MenuLayoutSettings.vue'
  import MenuStyleSettings from '@/components/core/layouts/art-settings-panel/widget/MenuStyleSettings.vue'
  import ColorSettings from '@/components/core/layouts/art-settings-panel/widget/ColorSettings.vue'
  import BoxStyleSettings from '@/components/core/layouts/art-settings-panel/widget/BoxStyleSettings.vue'
  import ContainerSettings from '@/components/core/layouts/art-settings-panel/widget/ContainerSettings.vue'
  import BasicSettings from '@/components/core/layouts/art-settings-panel/widget/BasicSettings.vue'

  defineOptions({ name: 'SystemSettings' })
</script>
