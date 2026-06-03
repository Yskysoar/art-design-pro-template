import request from '@/utils/http'

export function fetchSocialUsers(params: Api.Social.SocialSearchParams) {
  return request.get<Api.Social.SocialUserList>({
    url: '/api/social/users',
    params
  })
}

export function fetchSocialUserProfile(id: number) {
  return request.get<Api.Social.SocialUser>({
    url: `/api/social/users/${id}`
  })
}

export function fetchFollowing(params: Api.Social.SocialSearchParams) {
  return request.get<Api.Social.SocialUserList>({
    url: '/api/social/following',
    params
  })
}

export function fetchFollowers(params: Api.Social.SocialSearchParams) {
  return request.get<Api.Social.SocialUserList>({
    url: '/api/social/followers',
    params
  })
}

export function followUser(targetUserId: number) {
  return request.post<void>({
    url: `/api/social/follow/${targetUserId}`
  })
}

export function unfollowUser(targetUserId: number) {
  return request.del<void>({
    url: `/api/social/follow/${targetUserId}`
  })
}

export function fetchBlocks(params: Api.Social.SocialSearchParams) {
  return request.get<Api.Social.SocialUserList>({
    url: '/api/social/blocks',
    params
  })
}

export function blockUser(targetUserId: number) {
  return request.post<void>({
    url: `/api/social/block/${targetUserId}`
  })
}

export function unblockUser(targetUserId: number) {
  return request.del<void>({
    url: `/api/social/block/${targetUserId}`
  })
}

export function fetchConversations(params: Api.Social.SocialSearchParams) {
  return request.get<Api.Social.SocialConversationList>({
    url: '/api/social/conversations',
    params
  })
}

export function fetchMessages(conversationId: number, params: Api.Social.SocialMessageSearchParams) {
  return request.get<Api.Social.SocialMessageList>({
    url: `/api/social/conversations/${conversationId}/messages`,
    params
  })
}

export function sendSocialMessage(data: Api.Social.SocialMessageSendParams) {
  return request.post<Api.Social.SocialMessage>({
    url: '/api/social/messages',
    data,
    showErrorMessage: false
  })
}

export function markConversationRead(conversationId: number) {
  return request.post<void>({
    url: `/api/social/conversations/${conversationId}/read`
  })
}

export function fetchSocialUnreadCount() {
  return request.get<Api.Social.SocialUnreadCount>({
    url: '/api/social/unread-count'
  })
}
