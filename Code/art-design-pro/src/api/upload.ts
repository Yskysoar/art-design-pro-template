import request from '@/utils/http'

/**
 * 上传普通附件。
 */
export function uploadAttachment(file: File) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post<Api.Upload.UploadResponse>({
    url: '/api/common/upload/file',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
