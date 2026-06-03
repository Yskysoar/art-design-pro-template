<!-- 社交聊天页 -->
<template>
  <div class="social-chat page-content !p-0" :style="{ height: containerMinHeight }">
    <aside class="social-chat__side">
      <div class="side-toolbar">
        <ElInput
          v-model="keyword"
          placeholder="搜索用户"
          :prefix-icon="Search"
          clearable
          @keyup.enter="loadActiveList"
          @clear="loadActiveList"
        />
      </div>

      <ElTabs v-model="activeTab" stretch @tab-change="handleTabChange">
        <ElTabPane label="会话" name="conversations" />
        <ElTabPane label="关注" name="following" />
        <ElTabPane label="粉丝" name="followers" />
        <ElTabPane label="拉黑" name="blocks" />
      </ElTabs>

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
              <p>{{ item.lastMessage?.content || '暂无消息' }}</p>
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
                <ElTag v-if="item.mutualFollow" size="small" type="success">互关</ElTag>
                <ElTag v-else-if="item.following" size="small">已关注</ElTag>
                <ElTag v-else-if="item.blockedByMe" size="small" type="danger">已拉黑</ElTag>
              </div>
              <p>{{ item.email || item.userName }}</p>
            </div>
          </div>
        </template>

        <ElEmpty
          v-if="activeTab === 'conversations' ? conversations.length === 0 : users.length === 0"
          description="暂无数据"
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
            <ElButton v-if="!selectedUser.blockedByMe" :icon="ChatDotRound" @click="focusInput">私信</ElButton>
            <ElButton
              v-if="!selectedUser.blockedByMe"
              :type="selectedUser.following ? 'default' : 'primary'"
              @click="toggleFollow"
            >
              {{ selectedUser.following ? '取消关注' : '关注' }}
            </ElButton>
            <ElButton :type="selectedUser.blockedByMe ? 'warning' : 'danger'" plain @click="toggleBlock">
              {{ selectedUser.blockedByMe ? '解除拉黑' : '拉黑' }}
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
              <p>{{ message.content }}</p>
            </div>
          </div>
          <ElEmpty v-if="messages.length === 0" description="还没有消息" :image-size="110" />
        </ElScrollbar>

        <footer class="chat-input">
          <div class="quota-line" :class="{ danger: quotaDisabled }">
            <template v-if="selectedUser.blockedByMe">你已拉黑对方，不能发送私信</template>
            <template v-else-if="selectedUser.blockedMe">对方已拉黑你，不能发送私信</template>
            <template v-else-if="selectedConversation?.unlimited">已互相关注，可自由私信</template>
            <template v-else>对方回复前还可发送 {{ remainingQuota }} 条私信</template>
          </div>
          <ElInput
            ref="inputRef"
            v-model="messageText"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            resize="none"
            placeholder="输入消息"
            :disabled="quotaDisabled"
            @keyup.enter.prevent="sendMessage"
          />
          <div class="input-actions">
            <div class="input-tools">
              <ArtSvgIcon icon="ri:emotion-happy-line" />
              <ArtSvgIcon icon="ri:image-line" />
            </div>
            <ElButton type="primary" :disabled="quotaDisabled || !messageText.trim()" @click="sendMessage">
              发送
            </ElButton>
          </div>
        </footer>
      </template>

      <ElEmpty v-else class="chat-empty" description="选择一个用户开始聊天" />
    </main>
  </div>
</template>

<script setup lang="ts">
  import { ChatDotRound, Search } from '@element-plus/icons-vue'
  import { ElMessage } from 'element-plus'
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
    unblockUser,
    unfollowUser
  } from '@/api/social'
  import type { HttpError } from '@/utils/http/error'

  defineOptions({ name: 'SocialChat' })

  type TabName = 'conversations' | 'following' | 'followers' | 'blocks'

  const { containerMinHeight } = useAutoLayoutHeight()

  const activeTab = ref<TabName>('conversations')
  const keyword = ref('')
  const conversations = ref<Api.Social.SocialConversation[]>([])
  const users = ref<Api.Social.SocialUser[]>([])
  const messages = ref<Api.Social.SocialMessage[]>([])
  const selectedConversation = ref<Api.Social.SocialConversation | null>(null)
  const selectedUser = ref<Api.Social.SocialUser | null>(null)
  const messageText = ref('')
  const inputRef = ref()
  const messageScrollbarRef = ref()

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

  const relationText = computed(() => {
    if (!selectedUser.value) return ''
    if (selectedUser.value.blockedByMe) return '已拉黑'
    if (selectedUser.value.blockedMe) return '对方已拉黑你'
    if (selectedUser.value.mutualFollow) return '互相关注'
    if (selectedUser.value.following) return '我已关注'
    if (selectedUser.value.followedBy) return '对方关注了你'
    return '未关注'
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
      const message = await sendSocialMessage({ receiverId: selectedUser.value.id, content })
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
        ElMessage.error(`内容包含敏感词：${data.sensitiveWords.join('、')}`)
        return
      }
      ElMessage.error(httpError.message || '消息发送失败')
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

  function focusInput() {
    inputRef.value?.focus?.()
  }

  function scrollToBottom() {
    nextTick(() => {
      const wrap = messageScrollbarRef.value?.wrapRef
      if (wrap) wrap.scrollTop = wrap.scrollHeight
    })
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
    gap: 14px;
    color: var(--art-text-gray-600);
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

    .message-bubble {
      max-width: 82%;
    }

    .chat-input {
      padding: 12px;
    }
  }
</style>
