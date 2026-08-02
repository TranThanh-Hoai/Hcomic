/**
 * Data definition and initial mock dataset for Comic Entity in Hcomic.
 * 
 * Comic Entity Fields:
 * - id: Long (Auto-generated unique identifier)
 * - title: String (Required title of the comic)
 * - slug: String (URL-friendly slug generated from title)
 * - status: ComicStatus Enum ['ONGOING', 'COMPLETED', 'PAUSED', 'CANCELLED']
 * - createdAt: LocalDateTime / ISO String (Creation timestamp)
 * - updatedAt: LocalDateTime / ISO String (Last update timestamp)
 */

export const ComicStatus = {
  ONGOING: 'ONGOING',
  COMPLETED: 'COMPLETED',
  PAUSED: 'PAUSED',
  CANCELLED: 'CANCELLED'
};

export const initialComics = [
  {
    id: 1,
    title: "Solo Leveling",
    slug: "solo-leveling",
    status: ComicStatus.COMPLETED,
    createdAt: "2026-01-15T08:30:00.000Z",
    updatedAt: "2026-06-20T14:45:00.000Z"
  },
  {
    id: 2,
    title: "One Piece",
    slug: "one-piece",
    status: ComicStatus.ONGOING,
    createdAt: "2026-02-01T10:00:00.000Z",
    updatedAt: "2026-08-01T12:00:00.000Z"
  },
  {
    id: 3,
    title: "Tower of God",
    slug: "tower-of-god",
    status: ComicStatus.ONGOING,
    createdAt: "2026-02-10T09:15:00.000Z",
    updatedAt: "2026-07-28T16:20:00.000Z"
  },
  {
    id: 4,
    title: "Jujutsu Kaisen",
    slug: "jujutsu-kaisen",
    status: ComicStatus.COMPLETED,
    createdAt: "2026-03-05T11:45:00.000Z",
    updatedAt: "2026-07-30T18:10:00.000Z"
  },
  {
    id: 5,
    title: "Chainsaw Man",
    slug: "chainsaw-man",
    status: ComicStatus.ONGOING,
    createdAt: "2026-04-12T14:20:00.000Z",
    updatedAt: "2026-08-02T10:00:00.000Z"
  },
  {
    id: 6,
    title: "Hunter x Hunter",
    slug: "hunter-x-hunter",
    status: ComicStatus.PAUSED,
    createdAt: "2026-01-20T15:00:00.000Z",
    updatedAt: "2026-05-15T09:30:00.000Z"
  },
  {
    id: 7,
    title: "Vagabond",
    slug: "vagabond",
    status: ComicStatus.CANCELLED,
    createdAt: "2026-02-18T13:10:00.000Z",
    updatedAt: "2026-04-01T11:00:00.000Z"
  }
];

/**
 * Utility function to generate a slug from a title string
 * @param {string} title 
 * @returns {string} slugified title
 */
export function generateSlug(title) {
  if (!title) return '';
  return title
    .toLowerCase()
    .trim()
    .replace(/[^\w\s-]/g, '')
    .replace(/[\s_-]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

/**
 * Helper to format date strings for display
 * @param {string|Date} dateString 
 * @returns {string} formatted date
 */
export function formatDate(dateString) {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

export default {
  ComicStatus,
  initialComics,
  generateSlug,
  formatDate
};
