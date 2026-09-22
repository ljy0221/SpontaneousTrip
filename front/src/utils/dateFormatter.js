/**
 * 날짜 포맷팅 유틸리티
 * Date formatting utility
 */

/**
 * 날짜를 YYYY-MM-DD 형식으로 포맷팅
 * @param {Date|string|number} date - 변환할 날짜 (Date 객체, ISO 문자열, 또는 타임스탬프)
 * @returns {string} YYYY-MM-DD 형식의 날짜 문자열
 */
export const formatDate = (date) => {
  if (!date) return ''

  const d = new Date(date)

  // Invalid date 체크
  if (isNaN(d.getTime())) {
    console.warn('Invalid date provided to formatDate:', date)
    return ''
  }

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}
