import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Send push notification when a new message is created
 */
export const sendMessageNotification = functions.firestore
  .document("chats/{chatId}/messages/{messageId}")
  .onCreate(async (snap, context) => {
    const message = snap.data();
    const { chatId } = context.params;

    try {
      // Get recipient user data
      const recipientDoc = await db.collection("users").doc(message.toUid).get();
      if (!recipientDoc.exists) {
        console.log("Recipient user not found");
        return;
      }

      const recipientData = recipientDoc.data();
      const recipientFcmToken = recipientData?.fcmToken;

      if (!recipientFcmToken) {
        console.log("Recipient FCM token not found");
        return;
      }

      // Get sender user data
      const senderDoc = await db.collection("users").doc(message.fromUid).get();
      const senderData = senderDoc.data();
      const senderName = senderData?.name || "Alumni";

      // Check if recipient has message notifications enabled
      const notificationPrefs = recipientData?.notificationPreferences || {};
      if (notificationPrefs.messagesEnabled === false) {
        console.log("Message notifications disabled for recipient");
        return;
      }

      // Prepare notification payload
      const payload = {
        notification: {
          title: `New Message from ${senderName}`,
          body: message.messageText?.substring(0, 100) || "You have a new message",
          clickAction: "FLUTTER_NOTIFICATION_CLICK",
        },
        data: {
          chatId: chatId,
          fromUid: message.fromUid,
          type: "incoming_message",
          senderName: senderName,
        },
      };

      // Send notification
      await messaging.send({
        token: recipientFcmToken,
        notification: payload.notification,
        data: payload.data,
        android: {
          priority: "high",
          notification: {
            sound: "default",
            channelId: "messages",
          },
        },
        apns: {
          headers: {
            "apns-priority": "10",
          },
          payload: {
            aps: {
              sound: "default",
              badge: 1,
            },
          },
        },
      });

      console.log(`Notification sent to ${message.toUid}`);

      // Log to analytics
      await db.collection("notificationLogs").add({
        type: "message",
        recipientId: message.toUid,
        senderId: message.fromUid,
        chatId: chatId,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        status: "sent",
      });
    } catch (error) {
      console.error("Error sending notification:", error);
      
      // Log error
      await db.collection("notificationLogs").add({
        type: "message",
        recipientId: message.toUid,
        senderId: message.fromUid,
        chatId: chatId,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        status: "failed",
        error: error.message,
      });
    }
  });

/**
 * Send mentorship request notification
 */
export const sendMentorshipNotification = functions.firestore
  .document("mentorshipConnections/{connectionId}")
  .onCreate(async (snap, context) => {
    const connection = snap.data();

    try {
      // Get mentor user data
      const mentorDoc = await db.collection("users").doc(connection.mentorId).get();
      if (!mentorDoc.exists) {
        console.log("Mentor user not found");
        return;
      }

      const mentorData = mentorDoc.data();
      const mentorFcmToken = mentorData?.fcmToken;

      if (!mentorFcmToken) {
        console.log("Mentor FCM token not found");
        return;
      }

      // Get mentee data
      const menteeDoc = await db.collection("users").doc(connection.menteeId).get();
      const menteeData = menteeDoc.data();
      const menteeName = menteeData?.name || "Alumni";

      // Check if mentor has mentorship notifications enabled
      const notificationPrefs = mentorData?.notificationPreferences || {};
      if (notificationPrefs.mentorshipEnabled === false) {
        console.log("Mentorship notifications disabled for mentor");
        return;
      }

      // Send notification
      await messaging.send({
        token: mentorFcmToken,
        notification: {
          title: "New Mentorship Request",
          body: `${menteeName} has sent you a mentorship request`,
        },
        data: {
          connectionId: context.params.connectionId,
          type: "mentorship_request",
          menteeName: menteeName,
        },
        android: {
          priority: "high",
          notification: {
            sound: "default",
            channelId: "mentorship",
          },
        },
      });

      console.log(`Mentorship notification sent to ${connection.mentorId}`);
    } catch (error) {
      console.error("Error sending mentorship notification:", error);
    }
  });

/**
 * Send event update notification to all subscribers
 */
export const sendEventNotification = functions.firestore
  .document("events/{eventId}")
  .onUpdate(async (change, context) => {
    const newData = change.after.data();
    const oldData = change.before.data();

    // Only send if event details changed
    if (JSON.stringify(newData) === JSON.stringify(oldData)) {
      return;
    }

    try {
      // Get all users subscribed to events
      const usersSnapshot = await db
        .collection("users")
        .where("notificationPreferences.eventsEnabled", "==", true)
        .get();

      const tokens = [];
      usersSnapshot.forEach((doc) => {
        if (doc.data().fcmToken) {
          tokens.push(doc.data().fcmToken);
        }
      });

      if (tokens.length === 0) {
        console.log("No users subscribed to event notifications");
        return;
      }

      // Send multicast notification
      await messaging.sendMulticast({
        tokens: tokens,
        notification: {
          title: `Event Update: ${newData.title}`,
          body: newData.description?.substring(0, 100) || "Event has been updated",
        },
        data: {
          eventId: context.params.eventId,
          type: "event_update",
        },
        android: {
          priority: "high",
          notification: {
            sound: "default",
            channelId: "events",
          },
        },
      });

      console.log(`Event notification sent to ${tokens.length} users`);
    } catch (error) {
      console.error("Error sending event notification:", error);
    }
  });

/**
 * Send job posting notification to all subscribers
 */
export const sendJobNotification = functions.firestore
  .document("jobs/{jobId}")
  .onCreate(async (snap, context) => {
    const job = snap.data();

    try {
      // Get all users subscribed to job notifications
      const usersSnapshot = await db
        .collection("users")
        .where("notificationPreferences.jobsEnabled", "==", true)
        .get();

      const tokens = [];
      usersSnapshot.forEach((doc) => {
        if (doc.data().fcmToken) {
          tokens.push(doc.data().fcmToken);
        }
      });

      if (tokens.length === 0) {
        console.log("No users subscribed to job notifications");
        return;
      }

      // Send multicast notification
      await messaging.sendMulticast({
        tokens: tokens,
        notification: {
          title: `New Job: ${job.title}`,
          body: `${job.company} is hiring`,
        },
        data: {
          jobId: context.params.jobId,
          type: "job_posting",
        },
        android: {
          priority: "high",
          notification: {
            sound: "default",
            channelId: "jobs",
          },
        },
      });

      console.log(`Job notification sent to ${tokens.length} users`);
    } catch (error) {
      console.error("Error sending job notification:", error);
    }
  });
