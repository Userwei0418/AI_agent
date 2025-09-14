export const NOTIFICATION_READ_STATUS = {
  UNREAD: 0,
  READ: 1,
} as const;

export type NotificationReadStatus = typeof NOTIFICATION_READ_STATUS[keyof typeof NOTIFICATION_READ_STATUS];

export const NOTIFICATION_READ_MAP: Record<NotificationReadStatus, string> = {
  [NOTIFICATION_READ_STATUS.UNREAD]: '未读',
  [NOTIFICATION_READ_STATUS.READ]: '已读',
};
