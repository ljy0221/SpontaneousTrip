/**
 * 여행 계획 상태 변환 유틸리티
 * Travel plan status conversion utility
 */

/**
 * 한글 상태를 영어 상태로 변환 (백엔드 API 요청용)
 * @param {string} koreanStatus - 한글 상태 ("임시", "진행중", "완료")
 * @returns {string} 영어 상태 ("TEMP", "ONGOING", "DONE")
 */
export const statusToBackend = (koreanStatus) => {
  const mapping = {
    임시: 'TEMP',
    진행중: 'ONGOING',
    완료: 'DONE',
  }
  return mapping[koreanStatus] || koreanStatus
}

/**
 * 영어 상태를 한글 상태로 변환 (백엔드 응답 처리용)
 * @param {string} englishStatus - 영어 상태 ("TEMP", "ONGOING", "DONE")
 * @returns {string} 한글 상태 ("임시", "진행중", "완료")
 */
export const statusToFrontend = (englishStatus) => {
  const mapping = {
    TEMP: '임시',
    ONGOING: '진행중',
    DONE: '완료',
  }
  return mapping[englishStatus] || englishStatus
}
