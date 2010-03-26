
from utils.sipproc import SipProcess


class RequestParseNew(SipProcess):
    SHORT_DESCRIPTION = 'Parse new processes'

    def run(self):
        for mdRecord in MdRecord.items.all():
            if not Uri.items.filter(md_rec_id=mdRecord.id):
                u = Uri(md_rec_id=mdRecord.id)
                u.save()
        return


task_list = [RequestParseNew]
