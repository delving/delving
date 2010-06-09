import subprocess

def cmd_execute(cmd, cwd=''):
    "Returns 0 on success, or error message on failure."
    result = 0
    retcode, stdout, stderr = cmd_execute_output(cmd, cwd)
    if retcode:
        result = 'retcode: %s' % retcode
        if stdout:
            result += '\nstdout: %s' % stdout
        if stderr:
            result += '\nstderr: %s' % stderr
    return result



def cmd_execute_output(cmd, cwd=''):
    "Returns retcode,stdout,stderr."
    if isinstance(cmd, (list, tuple)):
        cmd = ' '.join(cmd)
    try:
        p = subprocess.Popen(cmd, shell=True, cwd=cwd,
                             stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        stdout, stderr = p.communicate()
        retcode = p.returncode
    except:
        retcode = 1
        stdout = ''
        stderr = 'cmd_execute() exception - shouldnt normally happen'
    return retcode, stdout, stderr

