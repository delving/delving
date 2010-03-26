"""
 Copyright 2010 EDL FOUNDATION

 Licensed under the EUPL, Version 1.1 or as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 you may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.


 Created by: Jacob Lundqvist (Jacob.Lundqvist@gmail.com)

 Maintains and runs tasks

"""

from optparse import make_option

from django.core.management.base import BaseCommand

from apps.process_monitor.processor import MainProcessor


class Command(BaseCommand):
    option_list = BaseCommand.option_list + (
        make_option('--single-run', action='store_true', dest='single-run', default=False,
            help='Process queue once then terminate.'),
    )
    help = """Runs all checks and keeps track of running processes.
    Also responsible for filesystem cleanup actions.

    Some extra modes, they will terminate when they are done.

    --single-run
       Perform all pendning tasks then exit

    """
    args = ''#[--daemon]'

    def handle(self, *args, **options):
        mp = MainProcessor(options['single-run'])
        mp.run()

