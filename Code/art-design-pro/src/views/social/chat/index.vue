<!-- 社交聊天页 -->
<template>
  <div class="social-chat page-content !p-0" :style="{ height: containerMinHeight }">
    <aside class="social-chat__side">
      <div class="side-toolbar">
        <ElInput
          v-model="keyword"
          :placeholder="$t('socialChat.searchUser')"
          :prefix-icon="Search"
          clearable
          @keyup.enter="loadActiveList"
          @clear="loadActiveList"
        />
      </div>

      <div class="side-tabs">
        <ElTabs v-model="activeTab" stretch @tab-change="handleTabChange">
          <ElTabPane :label="$t('socialChat.tabs.conversations')" name="conversations" />
          <ElTabPane :label="$t('socialChat.tabs.following')" name="following" />
          <ElTabPane :label="$t('socialChat.tabs.followers')" name="followers" />
          <ElTabPane :label="$t('socialChat.tabs.blocks')" name="blocks" />
        </ElTabs>
      </div>

      <ElScrollbar class="side-list">
        <template v-if="activeTab === 'conversations'">
          <div
            v-for="item in conversations"
            :key="item.id"
            class="side-item"
            :class="{ 'is-active': selectedConversation?.id === item.id }"
            @click="selectConversation(item)"
          >
            <ElBadge :value="item.unreadCount" :hidden="item.unreadCount === 0">
              <ElAvatar :size="42" :src="item.targetUser.avatar">{{ avatarText(item.targetUser) }}</ElAvatar>
            </ElBadge>
            <div class="side-item__main">
              <div class="side-item__top">
                <span>{{ displayName(item.targetUser) }}</span>
                <em>{{ formatShortTime(item.lastMessageTime) }}</em>
              </div>
              <p>{{ messagePreview(item.lastMessage) }}</p>
            </div>
          </div>
        </template>

        <template v-else>
          <div
            v-for="item in users"
            :key="item.id"
            class="side-item"
            :class="{ 'is-active': selectedUser?.id === item.id }"
            @click="selectUser(item)"
          >
            <ElAvatar :size="42" :src="item.avatar">{{ avatarText(item) }}</ElAvatar>
            <div class="side-item__main">
              <div class="side-item__top">
                <span>{{ displayName(item) }}</span>
                <ElTag v-if="item.mutualFollow" size="small" type="success">{{ $t('socialChat.relation.mutual') }}</ElTag>
                <ElTag v-else-if="item.following" size="small">{{ $t('socialChat.relation.following') }}</ElTag>
                <ElTag v-else-if="item.blockedByMe" size="small" type="danger">{{ $t('socialChat.relation.blocked') }}</ElTag>
              </div>
              <p>{{ item.email || item.userName }}</p>
            </div>
          </div>
        </template>

        <ElEmpty
          v-if="activeTab === 'conversations' ? conversations.length === 0 : users.length === 0"
          :description="$t('socialChat.empty.data')"
          :image-size="90"
        />
      </ElScrollbar>
    </aside>

    <main class="social-chat__main">
      <template v-if="selectedUser">
        <header class="chat-header">
          <div class="chat-user">
            <ElAvatar :size="44" :src="selectedUser.avatar">{{ avatarText(selectedUser) }}</ElAvatar>
            <div>
              <strong>{{ displayName(selectedUser) }}</strong>
              <span>{{ relationText }}</span>
            </div>
          </div>
          <div class="chat-actions">
            <ElButton
              v-if="!selectedUser.blockedByMe"
              :type="selectedUser.following ? 'default' : 'primary'"
              @click="toggleFollow"
            >
              {{ selectedUser.following ? $t('socialChat.actions.unfollow') : $t('socialChat.actions.follow') }}
            </ElButton>
            <ElButton :type="selectedUser.blockedByMe ? 'warning' : 'danger'" plain @click="toggleBlock">
              {{ selectedUser.blockedByMe ? $t('socialChat.actions.unblock') : $t('socialChat.actions.block') }}
            </ElButton>
          </div>
        </header>

        <ElScrollbar ref="messageScrollbarRef" class="message-scroll">
          <div v-for="message in messages" :key="message.id" class="message-row" :class="{ mine: message.mine }">
            <ElAvatar :size="34" :src="message.senderAvatar">{{ message.senderName.charAt(0) }}</ElAvatar>
            <div class="message-bubble">
              <div class="message-meta">
                <span>{{ message.senderName }}</span>
                <em>{{ message.createTime }}</em>
              </div>
              <p v-if="message.messageType === 'TEXT'">{{ message.content }}</p>
              <a
                v-else-if="message.messageType === 'IMAGE'"
                class="message-image"
                :href="message.fileUrl"
                target="_blank"
                rel="noopener noreferrer"
              >
                <img :src="message.fileUrl" :alt="message.fileName || $t('socialChat.imageMessage')" />
              </a>
              <a
                v-else
                class="message-file"
                :href="message.fileUrl"
                target="_blank"
                rel="noopener noreferrer"
              >
                <ArtSvgIcon icon="ri:file-3-line" />
                <span>{{ message.fileName || $t('socialChat.file') }}</span>
                <em>{{ formatFileSize(message.fileSize) }}</em>
              </a>
            </div>
          </div>
          <ElEmpty v-if="messages.length === 0" :description="$t('socialChat.empty.message')" :image-size="110" />
        </ElScrollbar>

        <footer class="chat-input">
          <div class="quota-line" :class="{ danger: quotaDisabled }">
            <template v-if="selectedUser.blockedByMe">{{ $t('socialChat.quota.blockedByMe') }}</template>
            <template v-else-if="selectedUser.blockedMe">{{ $t('socialChat.quota.blockedMe') }}</template>
            <template v-else-if="selectedConversation?.unlimited">{{ $t('socialChat.quota.unlimited') }}</template>
            <template v-else>{{ $t('socialChat.quota.remaining', { count: remainingQuota }) }}</template>
          </div>
          <ElInput
            v-model="messageText"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            resize="none"
            :placeholder="$t('socialChat.inputPlaceholder')"
            :disabled="quotaDisabled"
            @keyup.enter.prevent="sendMessage"
          />
          <div class="input-actions">
            <div class="input-tools">
              <ElPopover placement="top-start" :width="268" trigger="click">
                <template #reference>
                  <ElButton text :disabled="quotaDisabled" class="tool-button">
                    <ArtSvgIcon icon="ri:emotion-happy-line" />
                  </ElButton>
                </template>
                <div class="emoji-panel">
                  <button v-for="emoji in emojiList" :key="emoji" type="button" @click="appendEmoji(emoji)">
                    {{ emoji }}
                  </button>
                </div>
              </ElPopover>
              <ElButton text :disabled="mediaDisabled || uploading" class="tool-button" @click="selectImage">
                <ArtSvgIcon icon="ri:image-line" />
              </ElButton>
              <ElButton text :disabled="mediaDisabled || uploading" class="tool-button" @click="selectFile">
                <ArtSvgIcon icon="ri:attachment-2" />
              </ElButton>
              <input ref="imageInputRef" type="file" accept="image/*" class="hidden-input" @change="uploadImage" />
              <input ref="fileInputRef" type="file" class="hidden-input" @change="uploadFile" />
            </div>
            <ElButton type="primary" :loading="uploading" :disabled="quotaDisabled || !messageText.trim()" @click="sendMessage">
              {{ $t('socialChat.actions.send') }}
            </ElButton>
          </div>
        </footer>
      </template>

      <ElEmpty v-else class="chat-empty" :description="$t('socialChat.empty.selectUser')" />
    </main>
  </div>
</template>

<script setup lang="ts">
  import { Search } from '@element-plus/icons-vue'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import { useAutoLayoutHeight } from '@/hooks/core/useLayoutHeight'
  import {
    blockUser,
    fetchBlocks,
    fetchConversations,
    fetchFollowers,
    fetchFollowing,
    fetchMessages,
    followUser,
    markConversationRead,
    sendSocialMessage,
    uploadSocialFile,
    uploadSocialImage,
    unblockUser,
    unfollowUser
  } from '@/api/social'
  import type { HttpError } from '@/utils/http/error'

  defineOptions({ name: 'SocialChat' })

  type TabName = 'conversations' | 'following' | 'followers' | 'blocks'

  const { containerMinHeight } = useAutoLayoutHeight()
  const { t } = useI18n()

  const activeTab = ref<TabName>('conversations')
  const keyword = ref('')
  const conversations = ref<Api.Social.SocialConversation[]>([])
  const users = ref<Api.Social.SocialUser[]>([])
  const messages = ref<Api.Social.SocialMessage[]>([])
  const selectedConversation = ref<Api.Social.SocialConversation | null>(null)
  const selectedUser = ref<Api.Social.SocialUser | null>(null)
  const messageText = ref('')
  const messageScrollbarRef = ref()
  const imageInputRef = ref<HTMLInputElement>()
  const fileInputRef = ref<HTMLInputElement>()
  const uploading = ref(false)
  const emojiList = ['😀', '😄', '😂', '😊', '😍', '👍', '👏', '🙏', '🎉', '🔥', '💡', '✅', '❤️', '😎', '🤝', '💪']

  const selectedConversationForUser = computed(() => {
    if (!selectedUser.value) return null
    return conversations.value.find((item) => item.targetUser.id === selectedUser.value?.id) || null
  })

  const remainingQuota = computed(() => selectedConversation.value?.remainingQuota ?? 3)

  const quotaDisabled = computed(() => {
    if (!selectedUser.value) return true
    if (selectedUser.value.blockedByMe || selectedUser.value.blockedMe) return true
    if (selectedConversation.value?.unlimited) return false
    return remainingQuota.value <= 0
  })

  const mediaDisabled = computed(() => quotaDisabled.value || !selectedUser.value?.mutualFollow)

  const relationText = computed(() => {
    if (!selectedUser.value) return ''
    if (selectedUser.value.blockedByMe) return t('socialChat.relation.blockedByMe')
    if (selectedUser.value.blockedMe) return t('socialChat.relation.blockedMe')
    if (selectedUser.value.mutualFollow) return t('socialChat.relation.mutualFull')
    if (selectedUser.value.following) return t('socialChat.relation.iFollowed')
    if (selectedUser.value.followedBy) return t('socialChat.relation.followedBy')
    return t('socialChat.relation.none')
  })

  onMounted(async () => {
    await loadConversations()
  })

  async function handleTabChange() {
    await loadActiveList()
  }

  async function loadActiveList() {
    if (activeTab.value === 'conversations') {
      await loadConversations()
      return
    }
    const params = { current: 1, size: 50, keyword: keyword.value || undefined }
    const apiMap = {
      following: fetchFollowing,
      followers: fetchFollowers,
      blocks: fetchBlocks
    }
    users.value = (await apiMap[activeTab.value](params)).records
  }

  async function loadConversations() {
    const result = await fetchConversations({ current: 1, size: 50, keyword: keyword.value || undefined })
    conversations.value = result.records
    if (!selectedConversation.value && conversations.value.length > 0) {
      await selectConversation(conversations.value[0])
    }
  }

  async function selectConversation(conversation: Api.Social.SocialConversation) {
    selectedConversation.value = conversation
    selectedUser.value = conversation.targetUser
    await loadMessages(conversation.id)
    await markConversationRead(conversation.id)
    conversation.unreadCount = 0
  }

  async function selectUser(user: Api.Social.SocialUser) {
    selectedUser.value = user
    selectedConversation.value = selectedConversationForUser.value
    messages.value = []
    if (selectedConversation.value) {
      await loadMessages(selectedConversation.value.id)
      await markConversationRead(selectedConversation.value.id)
      selectedConversation.value.unreadCount = 0
    }
  }

  async function loadMessages(conversationId: number) {
    const result = await fetchMessages(conversationId, { current: 1, size: 50 })
    messages.value = result.records.slice().reverse()
    scrollToBottom()
  }

  async function sendMessage() {
    if (!selectedUser.value || quotaDisabled.value) return
    const content = messageText.value.trim()
    if (!content) return
    try {
      const message = await sendSocialMessage({ receiverId: selectedUser.value.id, messageType: 'TEXT', content })
      messages.value.push(message)
      messageText.value = ''
      await loadConversations()
      const current = conversations.value.find((item) => item.targetUser.id === selectedUser.value?.id)
      if (current) selectedConversation.value = current
      scrollToBottom()
    } catch (error) {
      const httpError = error as HttpError
      const data = httpError.data as { sensitiveWords?: string[]; remainingQuota?: number } | undefined
      if (data?.sensitiveWords?.length) {
        ElMessage.error(t('socialChat.errors.sensitive', { words: data.sensitiveWords.join('、') }))
        return
      }
      ElMessage.error(httpError.message || t('socialChat.errors.sendFailed'))
    }
  }

  async function toggleFollow() {
    if (!selectedUser.value) return
    if (selectedUser.value.following) {
      await unfollowUser(selectedUser.value.id)
    } else {
      await followUser(selectedUser.value.id)
    }
    await refreshSelectedUser()
  }

  async function toggleBlock() {
    if (!selectedUser.value) return
    if (selectedUser.value.blockedByMe) {
      await unblockUser(selectedUser.value.id)
    } else {
      await blockUser(selectedUser.value.id)
    }
    await refreshSelectedUser()
  }

  async function refreshSelectedUser() {
    await loadConversations()
    await loadActiveList()
    const userId = selectedUser.value?.id
    if (!userId) return
    const inConversation = conversations.value.find((item) => item.targetUser.id === userId)
    const inList = users.value.find((item) => item.id === userId)
    selectedConversation.value = inConversation || selectedConversation.value
    selectedUser.value = inConversation?.targetUser || inList || selectedUser.value
  }

  function scrollToBottom() {
    nextTick(() => {
      const wrap = messageScrollbarRef.value?.wrapRef
      if (wrap) wrap.scrollTop = wrap.scrollHeight
    })
  }

  function appendEmoji(emoji: string) {
    messageText.value += emoji
  }

  function selectImage() {
    imageInputRef.value?.click()
  }

  function selectFile() {
    fileInputRef.value?.click()
  }

  async function uploadImage(event: Event) {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ''
    if (!file || !selectedUser.value) return
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error(t('socialChat.errors.imageTooLarge'))
      return
    }
    if (!file.type.startsWith('image/')) {
      ElMessage.error(t('socialChat.errors.imageOnly'))
      return
    }
    await uploadAndSend(file, 'IMAGE')
  }

  async function uploadFile(event: Event) {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ''
    if (!file || !selectedUser.value) return
    if (file.size > 50 * 1024 * 1024) {
      ElMessage.error(t('socialChat.errors.fileTooLarge'))
      return
    }
    await uploadAndSend(file, 'FILE')
  }

  async function uploadAndSend(file: File, messageType: 'IMAGE' | 'FILE') {
    if (!selectedUser.value || mediaDisabled.value) return
    uploading.value = true
    try {
      const uploaded = messageType === 'IMAGE' ? await uploadSocialImage(file) : await uploadSocialFile(file)
      const message = await sendSocialMessage({
        receiverId: selectedUser.value.id,
        messageType,
        fileId: uploaded.id
      })
      messages.value.push(message)
      await loadConversations()
      const current = conversations.value.find((item) => item.targetUser.id === selectedUser.value?.id)
      if (current) selectedConversation.value = current
      scrollToBottom()
    } catch (error) {
      const httpError = error as HttpError
      ElMessage.error(httpError.message || t('socialChat.errors.fileFailed'))
    } finally {
      uploading.value = false
    }
  }

  function messagePreview(message?: Api.Social.SocialMessage) {
    if (!message) return t('socialChat.noMessage')
    if (message.messageType === 'IMAGE') return t('socialChat.imagePreview')
    if (message.messageType === 'FILE') return t('socialChat.filePreview', { name: message.fileName || '' }).trim()
    return message.content || t('socialChat.noMessage')
  }

  function formatFileSize(size?: number) {
    if (!size) return ''
    if (size < 1024) return `${size}B`
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
    return `${(size / 1024 / 1024).toFixed(1)}MB`
  }

  function displayName(user: Api.Social.SocialUser) {
    return user.nickName || user.userName
  }

  function avatarText(user: Api.Social.SocialUser) {
    return displayName(user).charAt(0)
  }

  function formatShortTime(time?: string) {
    if (!time) return ''
    return time.slice(5, 16)
  }
</script>

<style lang="scss" scoped>
  .social-chat {
    display: grid;
    grid-template-columns: clamp(260px, 24vw, 320px) minmax(0, 1fr);
    min-height: 0;
    overflow: hidden;
    background: var(--art-main-bg-color);
    border: 1px solid var(--art-card-border);
    border-radius: 8px;
  }

  .social-chat__side,
  .social-chat__main {
    min-width: 0;
    min-height: 0;
    background: var(--art-main-bg-color);
  }

  .social-chat__side {
    display: flex;
    flex-direction: column;
    border-right: 1px solid var(--art-card-border);
  }

  .side-toolbar {
    padding: 16px 16px 8px;
  }

  .side-tabs {
    padding: 0 12px;

    :deep(.el-tabs__header) {
      margin-bottom: 0;
    }

    :deep(.el-tabs__nav-wrap::after) {
      height: 1px;
    }

    :deep(.el-tabs__nav) {
      width: 100%;
    }

    :deep(.el-tabs__item) {
      flex: 1;
      justify-content: center;
      padding: 0 !important;
      text-align: center;
    }
  }

  .side-list {
    flex: 1;
    min-height: 0;
    padding: 8px 10px 14px;
  }

  .side-item {
    display: flex;
    gap: 12px;
    align-items: center;
    min-height: 68px;
    padding: 10px;
    margin-bottom: 4px;
    cursor: pointer;
    border-radius: 8px;

    &:hover,
    &.is-active {
      background: var(--art-gray-200);
    }
  }

  .side-item__main {
    flex: 1;
    min-width: 0;
  }

  .side-item__top {
    display: flex;
    gap: 8px;
    align-items: center;
    justify-content: space-between;
    min-width: 0;

    span {
      overflow: hidden;
      font-size: 14px;
      font-weight: 600;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    em {
      flex-shrink: 0;
      font-size: 12px;
      font-style: normal;
      color: var(--art-text-gray-600);
    }
  }

  .side-item p {
    margin: 4px 0 0;
    overflow: hidden;
    font-size: 12px;
    color: var(--art-text-gray-600);
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .social-chat__main {
    display: flex;
    flex-direction: column;
    min-width: 0;
    min-height: 0;
  }

  .chat-header {
    display: flex;
    gap: 12px;
    align-items: center;
    justify-content: space-between;
    padding: 16px 18px;
    border-bottom: 1px solid var(--art-card-border);
  }

  .chat-user {
    display: flex;
    gap: 12px;
    align-items: center;
    min-width: 0;

    strong,
    span {
      display: block;
    }

    strong {
      font-size: 16px;
    }

    span {
      margin-top: 4px;
      font-size: 12px;
      color: var(--art-text-gray-600);
    }
  }

  .chat-actions {
    display: flex;
    flex-shrink: 0;
    gap: 8px;
  }

  .message-scroll {
    flex: 1;
    min-height: 0;
    padding: 18px;
    background: var(--art-bg-color);
  }

  .message-row {
    display: flex;
    gap: 10px;
    align-items: flex-start;
    margin-bottom: 18px;

    &.mine {
      flex-direction: row-reverse;

      .message-bubble {
        align-items: flex-end;
      }

      .message-bubble p {
        background: rgb(var(--art-primary-rgb) / 12%);
      }

      .message-file {
        background: rgb(var(--art-primary-rgb) / 12%);
      }
    }
  }

  .message-bubble {
    display: flex;
    flex-direction: column;
    max-width: min(680px, 76%);
    min-width: 0;
  }

  .message-meta {
    display: flex;
    gap: 8px;
    margin-bottom: 5px;
    font-size: 12px;
    color: var(--art-text-gray-600);

    em {
      font-style: normal;
    }
  }

  .message-bubble p {
    padding: 10px 12px;
    margin: 0;
    overflow-wrap: anywhere;
    font-size: 14px;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
    background: var(--art-main-bg-color);
    border-radius: 8px;
  }

  .message-image {
    display: block;
    max-width: min(280px, 100%);
    overflow: hidden;
    border: 1px solid var(--art-card-border);
    border-radius: 8px;

    img {
      display: block;
      max-width: 100%;
      max-height: 220px;
      object-fit: cover;
    }
  }

  .message-file {
    display: grid;
    grid-template-columns: 28px minmax(0, 1fr);
    gap: 2px 10px;
    align-items: center;
    min-width: 210px;
    max-width: min(320px, 100%);
    padding: 10px 12px;
    color: inherit;
    text-decoration: none;
    background: var(--art-main-bg-color);
    border-radius: 8px;

    .art-svg-icon {
      grid-row: span 2;
      font-size: 24px;
      color: var(--theme-color);
    }

    span {
      overflow: hidden;
      font-size: 14px;
      font-weight: 600;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    em {
      font-size: 12px;
      font-style: normal;
      color: var(--art-text-gray-600);
    }
  }

  .chat-input {
    padding: 14px 18px 16px;
    border-top: 1px solid var(--art-card-border);
  }

  .quota-line {
    margin-bottom: 8px;
    font-size: 12px;
    color: var(--art-text-gray-600);

    &.danger {
      color: var(--el-color-danger);
    }
  }

  .input-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 10px;
  }

  .input-tools {
    display: flex;
    gap: 6px;
    color: var(--art-text-gray-600);
  }

  .tool-button {
    width: 32px;
    height: 32px;
    padding: 0;

    .art-svg-icon {
      font-size: 18px;
    }
  }

  .emoji-panel {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 4px;

    button {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      padding: 0;
      font-size: 18px;
      cursor: pointer;
      background: transparent;
      border: 0;
      border-radius: 6px;

      &:hover {
        background: var(--art-gray-200);
      }
    }
  }

  .hidden-input {
    display: none;
  }

  .chat-empty {
    height: 100%;
  }

  @media screen and (width <= 1280px) {
    .social-chat {
      grid-template-columns: clamp(240px, 26vw, 300px) minmax(0, 1fr);
    }
  }

  @media screen and (width <= 960px) {
    .social-chat {
      grid-template-columns: 1fr;
      overflow: auto;
    }

    .social-chat__side {
      height: min(320px, 42%);
      min-height: 220px;
      border-right: 0;
      border-bottom: 1px solid var(--art-card-border);
    }

    .social-chat__main {
      min-height: 0;
    }

    .chat-header {
      flex-direction: column;
      align-items: stretch;
    }

    .chat-actions {
      flex-wrap: wrap;
    }
  }

  @media screen and (width <= 640px) {
    .social-chat__side {
      height: 36%;
      min-height: 190px;
    }

    .side-tabs {
      padding: 0 10px;
    }

    .message-bubble {
      max-width: 82%;
    }

    .chat-input {
      padding: 12px;
    }
  }
</style>
