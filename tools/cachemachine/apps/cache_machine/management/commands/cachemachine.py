import os
import sys

from optparse import make_option

from django.core.management.base import BaseCommand, CommandError
import settings


class Command(BaseCommand):
    option_list = BaseCommand.option_list + (
        #make_option('--daemon', action='store_true', dest='daemon_mode', default=False,
        #    help='Forks and runs in background.'),
        #make_option('--forceremove', action='store_true', dest='force_remove', default=False,
        #    help='Removes pointer to running process.'),
        make_option('--create-dirs', action='store_true', dest='create-dirs', default=False,
            help='Creates all the hexdirs for output.'),
        make_option('--test-request', action='store_true', dest='test-request', default=False,
            help='Debugging, create a test request.'),
    )
    help = "Handles requests and maintains the cache."
    args = ''#[--daemon]'

    def handle(self, *args, **options):
        from utils import glob_consts
        from apps.cache_machine.models import ProcessMonitoring, Request
        import cmd.sune

        if args:
            raise CommandError('Usage is cachemachine %s' % self.args)

        already_running = ProcessMonitoring.objects.filter(role=glob_consts.PMR_REQ_HANDLER)

        if options['create-dirs']:
            self.create_old_hex_dirs(os.path.join(settings.MEDIA_ROOT,
                                                  settings.DIR_ORIGINAL))
        if 1:#options['force_remove']:
            if len(already_running):
                print 'Removing pointer to running process.'
                print '  Make sure you dont have one running!!'
                already_running.delete()
                print '  Succeeded to remove process from database.'
            else:
                pass
                #raise CommandError('Attempt to force remove, but no process is logged')
            #sys.exit()


        if options['test-request']:
            #q=Request.objects.all().delete()
            r = Request(provider='098', collection='09804',
                        fname='09804_Ag_EU_Bernstein_WZMA.xml',
                        fpath='requests_new/09804_Ag_EU_Bernstein_WZMA.xml',
                        sstate=glob_consts.ST_PENDING)
            r.save()
        if len(already_running):
            pm = already_running[0]
            raise CommandError('Already running as pid: %i' % pm.pid)
        p = ProcessMonitoring(role=glob_consts.PMR_REQ_HANDLER,
                              sstate=glob_consts.ST_INITIALIZING,
                              )
        p.save()
        cmd.sune.cachemachine_starter(p)


    def create_old_hex_dirs(self, base_dir):
        print 'Creating old style uppercase hexdirs, one level under', base_dir
        if not os.path.exists(base_dir):
            os.makedirs(base_dir)

        hexdigits = '0123456789ABCDEF'
        for c1 in hexdigits:
            for c2 in hexdigits:
                for c3 in hexdigits:
                    ddir = os.path.join(base_dir, c1 + c2 + c3)
                    if not os.path.exists(ddir):
                        os.mkdir(ddir)



