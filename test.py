import sys
import glob

def main():
	if len(sys.argv) != 2:
		print("Not enough arguments")
		return
	configFile = sys.argv[1]
	outputFiles = glob.glob('*-{file}'.format(file = configFile))

	csOutputs = []
	for filePath in outputFiles:
		file = open(filePath, 'r')
		for line in file.readlines():
			csOutputs.append(line)

		file.close()

	csOutputs.sort()

	ranges = []

	for csOutput in csOutputs:
		print(csOutput.split('\n')[0])
		rangeArr = (csOutput.split(': ')[0]).split('-')
		range = {
			'start': rangeArr[0],
			'end': rangeArr[1],
		}
		
		if len(ranges) != 0 and collidesWith(ranges[len(ranges) - 1], range):
			print('Mutual Exclusion Violated!')
			return
		else:
			ranges.append(range)
	print('Mutual Exclusion Upheld')

def collidesWith(first, second):
	if second['start'] < first['end']:
		return True


if __name__ == "__main__":
	main()