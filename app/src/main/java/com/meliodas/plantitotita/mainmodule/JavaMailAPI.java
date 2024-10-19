package com.meliodas.plantitotita.mainmodule;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.meliodas.plantitotita.R;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaMailAPI extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;
    private String mEmail;
    private String mSubject;
    private String mMessage;

    private AlertDialog sendingDialog;

    // Constructor
    public JavaMailAPI(Context context, String email, String subject, String message) {
        this.mContext = context;
        this.mEmail = email;
        this.mSubject = subject;
        this.mMessage = message;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            Properties props = createEmailProperties();
            Session session = createSession(props);

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(GmailUtils.EMAIL));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(mEmail));
            mimeMessage.setSubject(mSubject);
            mimeMessage.setText(mMessage);
            Transport.send(mimeMessage);

            return true; // Email sent successfully
        } catch (MessagingException e) {
            e.printStackTrace();
            return false; // Email not sent
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showDialog(R.layout.custom_alert_dialog_sending_feedback, null);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (sendingDialog != null && sendingDialog.isShowing()) {
            sendingDialog.dismiss();
        }

        showDialog(R.layout.custom_alert_dialog_feedback_thankyou, null);
    }

    private void showDialog(int layoutResId, View.OnClickListener buttonClickListener) {
        View view = LayoutInflater.from(mContext).inflate(layoutResId, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setView(view);

        AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        // Set button click listener if provided
        if (buttonClickListener != null) {
            Button button = view.findViewById(R.id.dialogContinueButton);
            if (button != null) {
                button.setOnClickListener(v -> {
                    alertDialog.dismiss();
                    buttonClickListener.onClick(v);
                });
            }
        } else {
            // Default action for the continue button to just dismiss the dialog
            Button continueButton = view.findViewById(R.id.dialogContinueButton);
            if (continueButton != null) {
                continueButton.setOnClickListener(v -> alertDialog.dismiss());
            }
        }

        alertDialog.show();

        // Save sending dialog for dismissal
        if (layoutResId == R.layout.custom_alert_dialog_sending_feedback) {
            sendingDialog = alertDialog;
        }
    }

    private Properties createEmailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        return props;
    }

    private Session createSession(Properties props) {
        return Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GmailUtils.EMAIL, GmailUtils.PASSWORD);
            }
        });
    }
}