import os

DEV_NULL = 'log.txt'
ANS_FILE = 'TestProgram'
JAR_NAME = 'Compiler.jar'
MARS_NAME = 'Mars_2021.jar'
FORMAT_INPUT = 'format_input.txt'
ADD_HEAD = '''
#include <stdio.h>

int getint() {
	int ret;
	scanf("%d", &ret);
	return ret;
}
'''

if __name__ == '__main__':
	diff = []
	fileId = []
	for i in os.listdir():
		spl = i.split('.')
		if len(spl) == 2 and spl[1] == 'txt' and spl[0].startswith('testfile'):
			try:
				fileId.append(int(spl[0][8:]))
			except:
				pass
	fileId.sort()
	for i in fileId:
		print('--------- running ' + 'testfile' + str(i) + ' ---------')
		with open('input' + str(i) + '.txt', 'r') as file:
			text = file.read()
		with open(FORMAT_INPUT, 'w') as file:
			for nums in text.split('\n'):
				for num in nums.strip().split(' '):
					file.write(num + '\n')
		if os.path.exists('testfile.txt'):
			os.remove('testfile.txt')
		os.rename('testfile' + str(i) + '.txt', 'testfile.txt')
		print('---> init finished')
		########################################################################
		os.system('java -jar ' + JAR_NAME + ' < ' + FORMAT_INPUT + ' > ' + DEV_NULL)
		name = 'mips' + str(i) + '.txt'
		if os.path.exists(name):
			os.remove(name)
		os.rename('mips.txt', name)
		print('---> run ' + JAR_NAME + ' finished')
		########################################################################
		with open('testfile.txt', 'r', errors='ignore') as file:
			code = file.read()
		with open(ANS_FILE + '.cpp', 'w') as file:
			file.write(ADD_HEAD + '\n' + code)
		os.system('g++ ' + ANS_FILE + '.cpp -o ' + ANS_FILE + ' -std=c++11')
		os.system('.\\' + ANS_FILE + ' < ' + FORMAT_INPUT + ' > answer' + str(i) + '.txt')
		print('---> run ' + ANS_FILE + '.cpp finished')
		########################################################################
		os.system('java -jar ' + MARS_NAME + ' mips' + str(i) + \
			'.txt < ' + FORMAT_INPUT + ' > simulate' + str(i) + '.txt')
		with open('simulate' + str(i) + '.txt', 'r') as file:
			text = file.read()
		text = text.split('\n')[2:]
		with open('simulate' + str(i) + '.txt', 'w') as file:
			file.write('\n'.join(text))
		print('---> simulate ObjectCode' + str(i) + ' finished')
		########################################################################
		with open('answer' + str(i) + '.txt', 'r') as file1:
			cont1 = file1.read().strip()
			# cont1 = file1.read()
		with open('simulate' + str(i) + '.txt', 'r') as file2:
			cont2 = file2.read().strip()
			# cont2 = file2.read()
		os.rename('testfile.txt', 'testfile' + str(i) + '.txt')
		if cont1 != cont2:
			diff.append('testfile' + str(i) + '.txt')
		print('---> compare finished, ' + str(cont1 == cont2))
		print('')
	os.remove(DEV_NULL)
	os.remove(FORMAT_INPUT)
	os.remove(ANS_FILE + '.cpp')
	os.remove(ANS_FILE + '.exe')
	while not os.path.exists('InstructionStatistics.txt'):
		pass
	os.remove('InstructionStatistics.txt')
	if len(diff) == 0:
		print('^^^ all same ^^^')
	else:
		print('diff at ' + str(diff))
