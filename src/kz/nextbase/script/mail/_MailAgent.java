package kz.nextbase.script.mail;

import kz.flabs.util.Util;
import kz.nextbase.script._Session;
import kz.nextbase.script.struct._Employer;
import kz.pchelka.env.Environment;
import kz.pchelka.messenger.InstMessengerAgent;
import kz.pchelka.reminder.Memo;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class _MailAgent {

    _Session session;

    public _MailAgent(_Session session) {
        this.session = session;
    }

    public ArrayList<String> organizeRecipients(ArrayList<String> recipients) {
        ArrayList<String> result = new ArrayList<>();

        for (String recipient : recipients) {
            if (recipient.startsWith("[") && recipient.endsWith("]")) {
                ArrayList<_Employer> ussersByRoles = session.getStructure().getAppUsersByRoles(recipient);
                for (_Employer ussersByRole : ussersByRoles) {
                    result.add(ussersByRole.getEmail());
                }
            } else {
                result.add(recipient);
            }
        }
        return result;
    }

    public boolean sendMail(ArrayList<String> recipients, _Memo m) {

        Memo memo = new Memo(Environment.defaultSender, organizeRecipients(recipients), m.msubject, m.body);
        return memo.send();
    }

    public boolean sendMail(String sender, String personal, ArrayList<String> recipients, _Memo m) {
        Memo memo = new Memo(sender, personal, organizeRecipients(recipients), m.msubject, m.body);
        return memo.send();
    }

    public boolean sendMail(ArrayList<String> recipients, ArrayList<String> JIDrecipients, _Memo m) {
        boolean isMemoSent;
        Memo memo = new Memo(Environment.defaultSender, organizeRecipients(recipients), m.msubject, m.body);

        isMemoSent = memo.send();

        InstMessengerAgent ima = new InstMessengerAgent();
        for (String receiver : JIDrecipients) {
            ima.sendMessage(receiver, Util.removeHTMLTags(m.body));
        }

        return isMemoSent;
    }

    public boolean sendMail(String sender, String personal, ArrayList<String> recipients, String subj, String body) {
        Memo memo = new kz.pchelka.reminder.Memo(sender, personal, organizeRecipients(recipients), subj, body);
        return memo.send();
    }

    public boolean sendMail(ArrayList<String> recipients, String subj, String body) {
        Memo memo = new Memo(Environment.defaultSender, organizeRecipients(recipients), subj, body);
        return memo.send();
    }

    public boolean sendMailAfter(ArrayList<String> recipients, String subj, String body) {
        final Memo memo = new Memo(Environment.defaultSender, organizeRecipients(recipients), subj, body);
        RunnableFuture<Boolean> f = new FutureTask<>(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return memo.send();
            }
        });
        new Thread(f).start();
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            Environment.logger.errorLogEntry(e);
            return false;
        }
    }

    public boolean sendMailAfter(String sender, String personal, ArrayList<String> recipients, String subj, String body) {
        final Memo memo = new kz.pchelka.reminder.Memo(sender, personal, organizeRecipients(recipients), subj, body);
        RunnableFuture<Boolean> f = new FutureTask<>(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return memo.send();
            }
        });
        new Thread(f).start();
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            Environment.logger.errorLogEntry(e);
            return false;
        }
    }

    public boolean sendMailAfter(ArrayList<String> recipients, _Memo m) {
        final Memo memo = new Memo(Environment.defaultSender, organizeRecipients(recipients), m.msubject, m.body);
        RunnableFuture<Boolean> f = new FutureTask<>(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return memo.send();
            }
        });
        new Thread(f).start();
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            Environment.logger.errorLogEntry(e);
            return false;
        }
    }

    public boolean sendMailAfter(ArrayList<String> recipients, final ArrayList<String> JIDrecipients, final _Memo m) {
        final Memo memo = new Memo(Environment.defaultSender, organizeRecipients(recipients), m.msubject, m.body);
        RunnableFuture<Boolean> f = new FutureTask<>(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                InstMessengerAgent ima = new InstMessengerAgent();
                for (String receiver : JIDrecipients) {
                    ima.sendMessage(receiver, Util.removeHTMLTags(m.body));
                }
                return memo.send();
            }
        });
        new Thread(f).start();
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            Environment.logger.errorLogEntry(e);
            return false;
        }
    }

}
