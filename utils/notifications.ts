import * as Notifications from 'expo-notifications';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldShowBanner: true,
    shouldShowList: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
  }),
});

export async function requestNotificationPermission(): Promise<boolean> {
  const { status: existingStatus } = await Notifications.getPermissionsAsync();
  let finalStatus = existingStatus;

  if (existingStatus !== 'granted') {
    const { status } = await Notifications.requestPermissionsAsync();
    finalStatus = status;
  }

  return finalStatus === 'granted';
}

export async function scheduleJobReminder(
  jobId: string,
  title: string,
  scheduledTime: Date
): Promise<string> {
  const secondsUntilReminder = Math.max(
    0,
    Math.floor((scheduledTime.getTime() - Date.now()) / 1000) - 1800
  );

  const trigger: Notifications.NotificationTriggerInput = {
    type: Notifications.SchedulableTriggerInputTypes.TIME_INTERVAL,
    seconds: secondsUntilReminder,
    repeats: false,
  };

  const notificationId = await Notifications.scheduleNotificationAsync({
    content: {
      title: 'Job Starting Soon',
      body: `"${title}" starts in 30 minutes`,
      data: { jobId },
      sound: true,
    },
    trigger,
  });

  return notificationId;
}

export async function cancelNotification(notificationId: string): Promise<void> {
  await Notifications.cancelScheduledNotificationAsync(notificationId);
}

export async function sendLocalNotification(title: string, body: string, data?: Record<string, string>) {
  await Notifications.scheduleNotificationAsync({
    content: {
      title,
      body,
      data,
      sound: true,
    },
    trigger: null,
  });
}
