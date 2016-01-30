package staff.handler.birthday_reminder

import kz.flabs.appenv.AppEnv
import kz.nextbase.script.*
import kz.nextbase.script.constants.*
import kz.nextbase.script.events._DoScheduledHandler
import kz.nextbase.script.mail._Memo
import kz.nextbase.script.struct._Employer

class Trigger extends _DoScheduledHandler {

	@Override
	public int doHandler(_Session session) {

		def memo
		def memo_test
		def calendar = Calendar.getInstance()
		def month = calendar.get(Calendar.MONTH)
		def day = calendar.get(Calendar.DAY_OF_MONTH)
		def date = new Date()
		def mAgent = session.getMailAgent()
		def allEmployers = session.getStructure().getAllEmployers()
		def employerbd = []
		def employersmsg = []
		def recipientEmail = []
		String one = "нашего сотрудника";
		String many = "наших сотрудников";
		// 3 массива сотрудников
		try {
			allEmployers.each { emp ->
				if ((emp.getBirthDate()?.toCalendar()?.get(Calendar.MONTH)?.equals(month)) && (emp.getBirthDate()?.toCalendar()?.get(Calendar.DAY_OF_MONTH)?.equals(day))) {
					println(emp.getBirthDate().toCalendar().get(Calendar.MONTH).equals(month))
					println(emp.getBirthDate().toCalendar().get(Calendar.DAY_OF_MONTH).equals(day))
					employerbd.push(emp)
				} else {
					employersmsg.push(emp)
				}
			}
			println(employerbd.size())
			println(employersmsg.size())
			String body = "Сегодня у " + (employerbd.size() == 2 ? one : many) + " День Рождения! " + "<br>"
			if (employerbd) {
				employerbd.each { _Employer emps ->
					body = "Сегодня у " + (employerbd.size() == 2 ? one : many) + " День Рождения! " + "<br>"
					employerbd.each { all ->
						if (emps != all) {
							body += all.getFullName() + "<br>   "
						}
					}
					recipientEmail.clear()
					recipientEmail.add(emps.getEmail())
					memo = new _Memo("Уведомление о Дне Рождения " + (employerbd.size() == 2 ? one : many), " Давайте поздравим с Днем Рождения!", body, null, true)
					mAgent.sendMail(recipientEmail, memo)
				}
				body = "Сегодня у " + (employerbd.size() == 1 ? one : many) + " День Рождения! " + "<br>"
				recipientEmail.clear()
				employerbd.each { _Employer emp ->
					body += emp.getFullName() + "<br>   "
				}

				recipientEmail.clear()
				employersmsg.each { _Employer emp ->
					recipientEmail.add(emp.getEmail())
				}
				memo = new _Memo("Уведомление о Дне Рождения " + (employerbd.size() == 1 ? one : many), " Давайте поздравим с Днем Рождения!", body, null, true)
				mAgent.sendMail(recipientEmail, memo)
			}
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);
		}

		println "test 1**********************************"
		return 0;
	}
}