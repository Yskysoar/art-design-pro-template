import request from '@/utils/http'

export function createContentReport(data: Api.Moderation.ContentReportCreateParams) {
  return request.post<number>({
    url: '/api/moderation/reports',
    data
  })
}

export function fetchContentReports(params: Api.Moderation.ContentReportSearchParams) {
  return request.get<Api.Moderation.ContentReportList>({
    url: '/api/moderation/reports',
    params
  })
}

export function handleContentReport(id: number, data: Api.Moderation.ContentReportHandleParams) {
  return request.request<void>({
    url: `/api/moderation/reports/${id}/handle`,
    method: 'PATCH',
    data
  })
}
