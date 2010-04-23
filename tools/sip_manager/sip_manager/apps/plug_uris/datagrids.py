# myapp/datagrids.py
from django.contrib.auth.models import User
from djblets.datagrid.grids import Column, DataGrid

import models

COL_NAME = 'name'
COL_WAITING = 'waiting'
COL_OK = 'imgs_ok'
COL_BAD = 'imgs_bad'

class UriSourcesDataGrid(DataGrid):
    #idd = Column("id", sortable=True)
    name = Column(COL_NAME, sortable=True)
    waiting = Column(COL_WAITING, sortable=True)
    imgs_ok = Column(COL_OK, sortable=True)
    imgs_bad = Column(COL_BAD, sortable=True)

    def __init__(self, request):
        sel_common = "SELECT COUNT(*) FROM plug_uris_uri WHERE"
        sql_table_join = "AND uri_source_id=plug_uris_urisource.id"

        sql_img_ok = "%s status=%i %s" % (sel_common, models.URIS_COMPLETED, sql_table_join)
        sql_img_waiting = "%s status=%i AND item_type=%i AND err_code=%i %s" % (
            sel_common, models.URIS_CREATED, models.URIT_OBJECT, models.URIE_NO_ERROR, sql_table_join)
        sql_img_bad = "%s err_code > %i %s" % (sel_common, models.URIE_NO_ERROR, sql_table_join)

        sql = "SELECT task_eta FROM process_monitor_processmonitoring where pid=plug_uris_urisource.pid"
        DataGrid.__init__(self, request,
                          models.UriSource.objects.extra(select={
                              'imgs_ok':sql_img_ok,
                              #'imgs_bad': sql_img_bad,
                              'imgs_waiting':sql_img_waiting,
                              #'eta':sql,
                              }), "Uri sources")
        #DataGrid.__init__(self, request, User.objects.filter(is_active=True), "Users")
        self.default_sort = [COL_NAME]
        self.default_columns = [COL_NAME, COL_WAITING, COL_OK, COL_BAD]



COL_PID = 'pid'
COL_NAME_IP = 'name_or_ip'


class UriSourcesDataGrid2(DataGrid):
    pid = Column(COL_PID, sortable=True)
    name_or_ip = Column(COL_NAME_IP, sortable=True)

    def __init__(self, request):
        DataGrid.__init__(self, request, models.UriSource.objects.all(), "UriSources")
        self.default_sort = [COL_NAME_IP]
        self.default_columns = [COL_NAME_IP, COL_PID]
