import sys, os, subprocess, lorun, shlex
import json, string
import signal
import datetime


class TimeOutException(Exception):
    pass


def compile(workPath, language, file_name, exec_file_name):
    def handle(signum, frame):
        raise TimeOutException("compile timeOut")

    language = language.lower()
    build_cmd = {
        "gcc": "gcc %s -o %s " % (os.path.join(workPath, file_name), os.path.join(workPath, exec_file_name)),
        "g++": "g++ %s -o %s " % (os.path.join(workPath, file_name), os.path.join(workPath, exec_file_name)),
        "java": "javac %s -d %s " % (os.path.join(workPath, file_name), workPath),
        "python": "python -m py_compile %s " % (os.path.join(workPath, file_name))
    }
    if language not in build_cmd.keys():
        return False
    fdout = open(os.path.join(workPath, "compilelog.out"), 'w')
    fderr = open(os.path.join(workPath, "compilelog.err"), 'w')
    p = subprocess.Popen(build_cmd[language], shell=True, stdout=fdout, stderr=fderr)
    try:
        signal.signal(signal.SIGALRM, handle)
        signal.alarm(5)
        out, err = p.communicate()
        signal.alarm(0)
        if p.returncode == 0:
            return True, None
        return False, get_text_file(os.path.join(workPath, "compilelog.err"))
    except TimeOutException, e:
        return False, "compile timeOut"
    except Exception, e:
        return False, "compile error"


def get_text_file(filename):
    if not os.path.exists(filename):
        return "ERROR: file not exit: %s" % (filename)
    if not os.path.isfile(filename):
        return "ERROR: %s not a filename." % (filename)
    file = open(filename, "r")
    content = ""
    i = 0
    while 1:
        line = file.readline()
        if not line:
            break
        i = i + 1
        content += line
        if i >= 50: break
        if len(content) > 10000: break
    file.close()
    return content


def run(workPath, index, language, testdata_path, standout_path, limitTime, limitMemory,exec_file_name):
    language = language.lower()
    cmd = ''
    if language == 'java':
        cmd = 'java -classpath %s %s ' % (workPath, exec_file_name)
    elif language == 'python':
        cmd = 'python %s ' % (os.path.join(workPath, exec_file_name))
    else:
        cmd = '%s ' % (os.path.join(workPath, exec_file_name))
    fin = open(testdata_path)
    ftemppath = os.path.join(workPath, str(index) + ".out")
    ftemp = open(ftemppath, 'w')
    runcfg = {
        'args': shlex.split(cmd),
        'fd_in': fin.fileno(),
        'fd_out': ftemp.fileno(),
        'timelimit': int(limitTime),
        'memorylimit': int(limitMemory)
    }
    res = lorun.run(runcfg)
    fin.close()
    ftemp.close()
    if res['result'] == 0:
        ftemp = open(ftemppath)
        fout = open(standout_path)
        res['result'] = lorun.check(fout.fileno(), ftemp.fileno())
        fout.close()
        ftemp.close()
    return res


def getResult(testdata_id, res, errMsg, useTime, useMemory):
    return {
        'testId': testdata_id,
        'status': res,
        'errMsg': errMsg,
        'usedTime': useTime,
        'usedMemory': useMemory,
    }


def judge(workPath, pid, testDataNum, language, limitTime, limitMemory, testdata_path, file_name, exec_file_name):
    compileRes, compileResMsg = compile(workPath, language, file_name, exec_file_name)
    ans = []
    if compileRes == True:
        testDataPath = os.path.join(testdata_path, pid)
        for i in range(0, string.atoi(testDataNum, 10), 1):
            if os.path.exists(testDataPath + '/' + str(i) + '.in') != True:
                ans.append(getResult(i, 10, 'Missing input file', -1, -1))
            elif os.path.exists(testDataPath + '/' + str(i) + '.out') != True:
                ans.append(getResult(i, 10, 'Missing output file', -1, -1))
            else:
                stdin_path = os.path.join(testDataPath, str(i) + '.in')
                stdout_path = os.path.join(testDataPath, str(i) + '.out')
                res = run(workPath, i, language, stdin_path, stdout_path, limitTime, limitMemory,exec_file_name)
                ans.append(getResult(i, res['result'] + 1, '', res['timeused'], res['memoryused']))
    else:
        ans.append(getResult(-1, 8, compileResMsg, -1, -1))
    return ans


if __name__ == '__main__':
    if len(sys.argv) != 10:
        exit(-1)
    else:
        jsonStr = ""
        workPath = sys.argv[1]
        pid = sys.argv[2]
        testDataDum = sys.argv[3]
        language = sys.argv[4]
        limitTime = sys.argv[5]
        limitMemory = sys.argv[6]
        testdata_path = sys.argv[7]
        file_name = sys.argv[8]
        exec_file_name = sys.argv[9]
        try:
            data = judge(workPath, pid, testDataDum, language, limitTime, limitMemory, testdata_path, file_name, exec_file_name)
            jsonStr = json.dumps(data, ensure_ascii=False)
            print jsonStr
        except Exception, e:
            jsonStr = str(e)
            print e
            pass
        finally:
            nowTime = datetime.datetime.now().strftime('%Y-%m-%d %H-%M-%S')
            resultFile = open(os.path.join(workPath, nowTime + ".result"), 'w')
            resultFile.write(jsonStr)
            pass
