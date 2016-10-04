//模仿Android 4.2.2源码中的EventHub.cpp

#include <jni.h>
#include <fcntl.h>
#include <dirent.h>
#include <pthread.h>
#include <android/keycodes.h>
#include "types.h"
#include "Debug.h"
#include "Input.h"
#include "linux_input.h"

#define INPUT_DEVICE_FILE_PATH "/dev/input"

/* this macro is used to tell if "bit" is set in "array"
 * it selects a byte from the array, and does a boolean AND
 * operation with a byte that only has the relevant bit set.
 * eg. to check for the 12th bit, we do (array[1] & 1<<4)
 */
#define test_bit(bit, array)    (array[bit/8] & (1<<(bit%8)))

#define sizeof_bit_array(bits)  ((bits + 7) / 8)

InputDevice* InputDevice::s_pInstance = NULL;

InputDevice& InputDevice::GetInstance()
{
	if(s_pInstance == NULL)
	{
		static Cleaner cl;
		s_pInstance = new InputDevice();
	}
	return *s_pInstance;
}

void InputDevice::DeleteInstance()
{
	if(s_pInstance)
	{
		delete s_pInstance;
		s_pInstance = NULL;
	}
}

//仿照android代码中的EventHub::scanDir()
InputDevice::InputDevice()
{
	m_iKeyboardFDNumber = m_iTouchScreenFDNumber = 0;
	memset(&m_InputDevFD, 0, sizeof(m_InputDevFD));

	m_pTouchInfo = NULL;
	m_ProtoType = PROTO_TYPE_UNKNOW; //PROTO_TYPE_UNKNOW表示协议类型未知
	ClearTouchInfoArray(m_MultiTouchPointInfo);
	BuildScanCodeMapping();
	
	if(pipe(m_ThreadEventPipe) < 0)
		return;

	m_InputDevFD.EventPipe.fd = m_ThreadEventPipe[0];
	m_InputDevFD.EventPipe.events = POLLIN;

	//搜索/dev/input目录
	DIR* dir = opendir(INPUT_DEVICE_FILE_PATH);
	if(dir == NULL)
		return;

	struct dirent *de;
	while(de = readdir(dir))
	{	//排除.和..文件
		if(de->d_name[0] == '.' && (de->d_name[1] == '\0' || (de->d_name[1] == '.' && de->d_name[2] == '\0')))
			continue;
		
		char lpszPathName[PATH_MAX] = INPUT_DEVICE_FILE_PATH"/";
		strcat(lpszPathName, de->d_name);

		//尝试打开文件
		int fd = open(lpszPathName, O_RDWR);
		if(fd < 0)
			continue;

		//如果是键盘或触摸屏设备，则把fd记录下来，否则关闭fd
		auto iType = AddDevice(fd);
		if(iType)
		{
			LOGI("found device type = %d path = %s", iType, lpszPathName);
		}
		else
		{
			close(fd);
		}
	}
	closedir(dir);

	int iAllInputFDNumber = 0;
	m_pAllInputFD = new pollfd[1 + m_iKeyboardFDNumber + m_iTouchScreenFDNumber];	//1个管道，一组键盘，一组触摸屏
	m_pAllInputFD[iAllInputFDNumber++] = m_InputDevFD.EventPipe;
	for(auto i = 0; i < m_iKeyboardFDNumber; i++)
	{
		m_pAllInputFD[iAllInputFDNumber++] = m_InputDevFD.m_KeyboardPollFD[i];
	}

	for(auto i = 0; i < m_iTouchScreenFDNumber; i++)
	{
		m_pAllInputFD[iAllInputFDNumber++] = m_InputDevFD.m_TouchScreenPollFD[i];
	}
}

InputDevice::~InputDevice()
{
	LOGI("InputDevice::~InputDevice()");

	Cancel();
	close(m_ThreadEventPipe[1]);

	for(auto i = 0; i < m_iKeyboardFDNumber; i++) 
	{
		close(m_InputDevFD.m_KeyboardPollFD[i].fd);
	}

	for(auto i = 0; i < m_iTouchScreenFDNumber; i++) 
	{
		close(m_InputDevFD.m_TouchScreenPollFD[i].fd);
	}

	m_iKeyboardFDNumber = m_iTouchScreenFDNumber = 0;
	memset(&m_InputDevFD, 0, sizeof(m_InputDevFD));

	if(m_pTouchInfo)
	{
		delete [] m_pTouchInfo;
		m_pTouchInfo = NULL;
	}

	if(m_pAllInputFD)
	{
		delete [] m_pAllInputFD;
		m_pAllInputFD = NULL;
	}
	ClearTouchInfoArray(m_MultiTouchPointInfo);
}

bool InputDevice::containsNonZeroByte(const uint8_t* array, uint32_t startIndex, uint32_t endIndex) 
{	//从android源码中照搬
	const uint8_t* end = array + endIndex;
	array += startIndex;
	while (array != end) 
	{
		if (*(array++) != 0) 
		{
			return true;
		}
	}
	return false;
}

void InputDevice::BuildScanCodeMapping()
{
	UINT KeyCodeMapping[][2] = 
	{
		AKEYCODE_HOME,			KEY_HOME,
		AKEYCODE_BACK,			KEY_BACK,
		AKEYCODE_CALL,			0,
		AKEYCODE_ENDCALL,		0,
		AKEYCODE_0,			 	KEY_0,
		AKEYCODE_1,			 	KEY_1,
		AKEYCODE_2,			 	KEY_2,
		AKEYCODE_3,			 	KEY_3,
		AKEYCODE_4,			 	KEY_4,
		AKEYCODE_5,			 	KEY_5,
		AKEYCODE_6,			 	KEY_6,
		AKEYCODE_7,			 	KEY_7,
		AKEYCODE_8,			 	KEY_8,
		AKEYCODE_9,			 	KEY_9,
		AKEYCODE_VOLUME_UP,		KEY_VOLUMEUP,
		AKEYCODE_VOLUME_DOWN,	KEY_VOLUMEDOWN,
		AKEYCODE_POWER,			KEY_POWER,
		AKEYCODE_CAMERA,		KEY_CAMERA,
		AKEYCODE_A,				KEY_A,
		AKEYCODE_B,				KEY_B,
		AKEYCODE_C,				KEY_C,
		AKEYCODE_D,				KEY_D,
		AKEYCODE_E,				KEY_E,
		AKEYCODE_F,				KEY_F,
		AKEYCODE_G,				KEY_G,
		AKEYCODE_H,				KEY_H,
		AKEYCODE_I,				KEY_I,
		AKEYCODE_J,				KEY_J,
		AKEYCODE_K,				KEY_K,
		AKEYCODE_L,				KEY_L,
		AKEYCODE_M,				KEY_M,
		AKEYCODE_N,				KEY_N,
		AKEYCODE_O,				KEY_O,
		AKEYCODE_P,				KEY_P,
		AKEYCODE_Q,				KEY_Q,
		AKEYCODE_R,				KEY_R,
		AKEYCODE_S,				KEY_S,
		AKEYCODE_T,				KEY_T,
		AKEYCODE_U,				KEY_U,
		AKEYCODE_V,				KEY_V,
		AKEYCODE_W,				KEY_W,
		AKEYCODE_X,				KEY_X,
		AKEYCODE_Y,				KEY_Y,
		AKEYCODE_Z,				KEY_Z,
		AKEYCODE_COMMA,			KEY_COMMA,
		AKEYCODE_PERIOD,		0,
		AKEYCODE_TAB,			KEY_TAB,
		AKEYCODE_SPACE,			KEY_SPACE,
		AKEYCODE_ENTER,			KEY_ENTER,
		AKEYCODE_DEL,			KEY_DELETE,
		AKEYCODE_MINUS,			KEY_MINUS,
		AKEYCODE_EQUALS,		KEY_EQUAL,
		AKEYCODE_LEFT_BRACKET,	KEY_LEFTBRACE,
		AKEYCODE_RIGHT_BRACKET, KEY_RIGHTBRACE,
		AKEYCODE_BACKSLASH,		KEY_BACKSLASH,
		AKEYCODE_SEMICOLON,		KEY_SEMICOLON,
		AKEYCODE_SLASH,			KEY_SLASH,
		AKEYCODE_AT,			0,
		AKEYCODE_PLUS,			0,
		AKEYCODE_MENU,			KEY_MENU,
		AKEYCODE_PAGE_UP,		KEY_PAGEUP,
		AKEYCODE_PAGE_DOWN,		KEY_PAGEDOWN,
	};
	const int KeyCodeMappingSize = sizeof(KeyCodeMapping) / sizeof(KeyCodeMapping[0]);

	memset(m_KeyboardScanCodeMapping, 0, sizeof(m_KeyboardScanCodeMapping));

	for(int i = 0; i < KeyCodeMappingSize; i++)
	{
		if(KeyCodeMapping[i][1] < MAX_KEY_NUMBER)
			m_KeyboardScanCodeMapping[KeyCodeMapping[i][1]] = (BYTE)KeyCodeMapping[i][0];
	}
}

int InputDevice::AddDevice(int fd)
{	
	uint8_t keyBitmask[(KEY_MAX + 1) / 8] = {0};
	uint8_t absBitmask[(ABS_MAX + 1) / 8] = {0};
		
	ioctl(fd, EVIOCGBIT(EV_KEY, sizeof(keyBitmask)), keyBitmask);
	ioctl(fd, EVIOCGBIT(EV_ABS, sizeof(absBitmask)), absBitmask);
  
	int iDeviceType = 0;
  
  //判断是否是键盘设备
	// See if this is a keyboard.  Ignore everything in the button range except for
	// joystick and gamepad buttons which are handled like keyboards for the most part.
	bool haveKeyboardKeys = containsNonZeroByte(keyBitmask, 0, sizeof_bit_array(BTN_MISC))
            || containsNonZeroByte(keyBitmask, sizeof_bit_array(KEY_OK),
                    sizeof_bit_array(KEY_MAX + 1));
	bool haveGamepadButtons = containsNonZeroByte(keyBitmask, sizeof_bit_array(BTN_MISC),
                    sizeof_bit_array(BTN_MOUSE))
            || containsNonZeroByte(keyBitmask, sizeof_bit_array(BTN_JOYSTICK),
                    sizeof_bit_array(BTN_DIGI));
	if (haveKeyboardKeys || haveGamepadButtons) 
	{
		iDeviceType = 1;		//键盘设备
	}

  //判断是否是触摸屏设备
	// See if this is a touch pad.
	// Is this a new modern multi-touch driver?
	if (test_bit(ABS_MT_POSITION_X, absBitmask)
            && test_bit(ABS_MT_POSITION_Y, absBitmask)) 
	{
		// Some joysticks such as the PS3 controller report axes that conflict
		// with the ABS_MT range.  Try to confirm that the device really is
		// a touch screen.
		if (test_bit(BTN_TOUCH, keyBitmask) || !haveGamepadButtons) 
		{
			iDeviceType = 2;		//多点触摸屏设备
		}
	}
	else if(test_bit(BTN_TOUCH, keyBitmask) && test_bit(ABS_X, absBitmask) && test_bit(ABS_Y, absBitmask)) 
	{	// Is this an old style single-touch driver?
		//暂不考虑单点触摸设备
		//iDeviceType = 2;		//单点触摸屏设备
		LOGI("------>>>>>This is an old style single-touch driver");
	}

	if(iDeviceType == 1 && m_iKeyboardFDNumber < MAX_FD_NUMBER)
	{
		m_InputDevFD.m_KeyboardPollFD[m_iKeyboardFDNumber].fd = fd;
		m_InputDevFD.m_KeyboardPollFD[m_iKeyboardFDNumber].events = POLLIN;
		m_iKeyboardFDNumber++;
	}

	if(iDeviceType == 2 && m_iTouchScreenFDNumber < MAX_FD_NUMBER)
	{
		m_InputDevFD.m_TouchScreenPollFD[m_iTouchScreenFDNumber].fd = fd;
		m_InputDevFD.m_TouchScreenPollFD[m_iTouchScreenFDNumber].events = POLLIN;
		m_iTouchScreenFDNumber++;
	}
	
	return iDeviceType;
}

int InputDevice::ReadInput(bool bGetKeyDownOnly, bool bIsIncludeTouchScreenDevice, int ReturnValues[])
{
	LOGI("ReadInput(include touch screen = %d) begin", bIsIncludeTouchScreenDevice);
	bool bRet = false;
	int iPointId = -1;
	struct pollfd *AllInputFD = m_pAllInputFD;
	int iAllInputFDNumber = 0;
	
	if(bIsIncludeTouchScreenDevice == false)
		iAllInputFDNumber = 1 + m_iKeyboardFDNumber;
	else
		iAllInputFDNumber = 1 + m_iKeyboardFDNumber + m_iTouchScreenFDNumber;

	while(1)
	{	//当getevent的value不为1的时候，继续等待检测
		//等待所有的键盘设备和一个用于检测退出的管道
		auto pollres = poll(AllInputFD, iAllInputFDNumber, -1);

		if(AllInputFD[0].revents & POLLIN)
		{	//如果是管道（下标为0）可读了，说明已经发出了退出消息
			close(m_ThreadEventPipe[0]);
			LOGI("Read Input Thread Need Quit");
			return -1;
		}
		
		for(auto i = 0; i < m_iKeyboardFDNumber; i++)
		{
			if(AllInputFD[1 + i].revents & POLLIN) 
			{
				struct input_event event;
				auto res = read(AllInputFD[1 + i].fd, &event, sizeof(event));
				if(res < (int)sizeof(event))
				{	//读出错
					continue;
				}
				if(event.type == EV_KEY)
				{	//按下消息
					LOGI("from %d get key event %d %d(%d) %d", i, event.type, event.code, m_KeyboardScanCodeMapping[event.code], event.value);

					if(bGetKeyDownOnly && event.value != 1)
						continue;

					//第一个字节如果小于100，说明是键盘消息
					ReturnValues[0] = i;
					ReturnValues[1] = m_KeyboardScanCodeMapping[event.code];
					ReturnValues[2] = event.value;
					ReturnValues[3] = 0;
					ReturnValues[4] = 0;

					return event.code;
				}
			}
		}

		for(auto i = 0; i < m_iTouchScreenFDNumber; i++)
		{
			if(m_pTouchInfo == NULL)
				m_pTouchInfo = new STouchInfo[m_iTouchScreenFDNumber];

			int iReturnValue = -1;

			if(AllInputFD[1 + m_iKeyboardFDNumber + i].revents & POLLIN) 
			{
				struct input_event event;
				auto res = read(AllInputFD[1 + m_iKeyboardFDNumber + i].fd, &event, sizeof(event));
				if(res < (int)sizeof(event))
				{	//读出错
					continue;
				}

				UINT &rCurrentSlot = m_pTouchInfo[i].m_nCurrentSlot;
				rCurrentSlot = rCurrentSlot % MAX_SLOT_NUMBER;
				STouchInfo::STouchInfoSlot *pSlot = &(m_pTouchInfo[i].m_Slot[rCurrentSlot]);

				if(event.type == EV_ABS)
				{	
					switch(event.code)
					{
					case ABS_MT_SLOT:
						if(pSlot->bIsModified)
						{
							LOGI("ABS_MT_SLOT Dev %d, Slot %d, x=%d, y=%d, id=%d", i, rCurrentSlot, pSlot->iX, pSlot->iY, pSlot->iTrackingId);
							iReturnValue = ReturnValues[0] = i + 100;		//第一个字节如果大于100，说明是触摸屏消息
							ReturnValues[1] = rCurrentSlot;
							ReturnValues[2] = pSlot->iX;
							ReturnValues[3] = pSlot->iY;
							ReturnValues[4] = pSlot->iTrackingId;

							if(ReturnValues[4] < 0)
								pSlot->Clear();
							pSlot->bIsModified = false;
						}

						rCurrentSlot = event.value;
						break;
					case ABS_MT_POSITION_X:
					//case ABS_X:
						pSlot->iX = event.value;
						pSlot->bIsModified = true;
						break;
					case ABS_MT_POSITION_Y:
					//case ABS_Y:
						pSlot->iY = event.value;
						pSlot->bIsModified = true;
						break;
					case ABS_MT_TRACKING_ID:
						pSlot->iTrackingId = event.value;
						pSlot->bIsModified = true;
						break;
					}
				}
				else if(event.type == EV_SYN)
				{
					switch(event.code)
					{
					case SYN_MT_REPORT:
						m_ProtoType = PROTO_TYPE_A;
						AddPointToTouchInfoArray(m_pTouchInfo[i].m_CurTouchInfo, pSlot);
						bRet = IsTouchPointMove(m_pTouchInfo[i].m_LatestTouchInfo, pSlot);//只有该点发生移动了才往上返馈
						if(pSlot->bIsModified && bRet)
						{
							LOGI("SYN_MT_REPORT (Move) Dev %d, Slot %d, x=%d, y=%d, id=%d", i, rCurrentSlot, pSlot->iX, pSlot->iY, pSlot->iTrackingId);
							iReturnValue = ReturnValues[0] = i + 100;
							ReturnValues[1] = pSlot->iTrackingId;//手指ID
							ReturnValues[2] = pSlot->iX;
							ReturnValues[3] = pSlot->iY;
							ReturnValues[4] = pSlot->iTrackingId;//-1表示该手机抬起

							if(ReturnValues[4] < 0)
								pSlot->Clear();
							pSlot->bIsModified = false;
						}
						break;
					case SYN_REPORT:
						if (PROTO_TYPE_A == m_ProtoType)
						{
							LOGI("PROTO_TYPE_A SYN_REPORT Dev %d, Slot %d, x=%d, y=%d, id=%d", i, rCurrentSlot, pSlot->iX, pSlot->iY, pSlot->iTrackingId);
							//若是在type A模式下,收到SYN_REPORT消息，需确定是否有手指抬起
							iPointId = GetTouchUpPoint(m_pTouchInfo[i].m_CurTouchInfo, m_pTouchInfo[i].m_LatestTouchInfo);
							if (-1 != iPointId)
							{
								iReturnValue = ReturnValues[0] = i + 100;
								ReturnValues[1] = iPointId;//手指ID
								ReturnValues[2] = 0;
								ReturnValues[3] = 0;
								ReturnValues[4] = -1;//-1表示该手机抬起
							}
							//将当前的触点信息数组复制到最近一次的触点信息数组中，并清空当前的触点信息数组
							CopyTouchInfoArray(m_pTouchInfo[i].m_CurTouchInfo, m_pTouchInfo[i].m_LatestTouchInfo);
						}
						else
						{
							m_ProtoType = PROTO_TYPE_B;
							if(pSlot->bIsModified)
							{
								LOGI("PROTO_TYPE_B SYN_REPORT Dev %d, Slot %d, x=%d, y=%d, id=%d", i, rCurrentSlot, pSlot->iX, pSlot->iY, pSlot->iTrackingId);
								iReturnValue = ReturnValues[0] = i + 100;
								ReturnValues[1] = rCurrentSlot;
								ReturnValues[2] = pSlot->iX;
								ReturnValues[3] = pSlot->iY;
								ReturnValues[4] = pSlot->iTrackingId;
								
								//此处不能清理pSlot的内容，因为就算该手指弹起了，也有可能会出现以下这种情况，从而会出现touchdownevent命令中坐标为-1的情况：
// 								[    7541.046314] EV_ABS       ABS_MT_TOUCH_MAJOR   00000001
// 								[    7541.046314] EV_ABS       ABS_MT_PRESSURE      0000000c
// 								[    7541.046344] EV_SYN       SYN_REPORT           00000000
// 								[    7541.066457] EV_ABS       ABS_MT_TRACKING_ID   ffffffff   //手指抬起
// 								[    7541.066457] EV_SYN       SYN_REPORT           00000000
// 
// 								[    7541.155394] EV_ABS       ABS_MT_TRACKING_ID   000006cd
// 								[    7541.155394] EV_ABS       ABS_MT_TOUCH_MAJOR   00000003
// 								[    7541.155424] EV_ABS       ABS_MT_POSITION_Y    00000423	//只有Y坐标更新了，X坐标没有更新的事件
// 								[    7541.155424] EV_ABS       ABS_MT_PRESSURE      0000001d
// 								[    7541.155455] EV_SYN       SYN_REPORT           00000000

// 								if(ReturnValues[4] < 0)
// 									pSlot->Clear();
								pSlot->bIsModified = false;
							}
						}
						break;
					}
				}
				else if(event.type == EV_KEY)
				{
					switch(event.code)
					{
					case BTN_TOUCH:
						if(event.value == 1)	//down
						{
						}
						else if(event.value == 0)	//up
						{
							pSlot->iTrackingId = -1;
							pSlot->bIsModified = true;
						}
						break;
					case KEY_HOME:
					case KEY_BACK:
					case KEY_MENU:
						//适配小米2S的实体按键
						ReturnValues[0] = i;
						ReturnValues[1] = m_KeyboardScanCodeMapping[event.code];
						ReturnValues[2] = event.value;
						ReturnValues[3] = 0;
						ReturnValues[4] = 0;
						pSlot->bIsModified = false;
						return event.code;
						break;
					default:
						break;
					}
				}
			}		//if touch screen message comes

			if(iReturnValue >= 0)
				return iReturnValue;
		}	//for all touch screen devices
	}	//main loop

	return 0;
}

int InputDevice::ReadKeyboard(bool bGetKeyDownOnly)
{
	int Dummy[5];
	bool bIsIncludeTouchScreenDevice = false;
	if (PROTO_TYPE_UNKNOW == m_ProtoType){
		bIsIncludeTouchScreenDevice = true;
	}else{
		bIsIncludeTouchScreenDevice = false;
		LOGI("-----m_ProtoType = %d", m_ProtoType);
	}
	return ReadInput(bGetKeyDownOnly, bIsIncludeTouchScreenDevice, Dummy);
}

int InputDevice::WritePointerInput(int iAction, int iX, int iY, int iFingerId, int iTime)
{
	const UINT nMaxFingerId = 10;

	if(m_iTouchScreenFDNumber <=0)
		return -1;

	if((UINT)iFingerId >= nMaxFingerId)
		return -1;

	auto iPointerFD = m_pAllInputFD[1 + m_iKeyboardFDNumber].fd;
	if (PROTO_TYPE_A == m_ProtoType)
	{//Type A模式
		WritePointerInputTypeA(iPointerFD, iAction, iX, iY, iFingerId);
	} 
	else
	{//默认为Type B模式
		static int iTrackingIdAllocated = 32768, iTrackingId[nMaxFingerId] = {0};

		LOGI("InputDevice::WritePointerInput %d %d finger=%d", iX, iY, iFingerId);
		switch(iAction)
		{
		case 0:		//touch_down
			WriteEvent(iPointerFD, EV_ABS, ABS_MT_SLOT, iFingerId);
			iTrackingId[iFingerId] = iTrackingIdAllocated;
			iTrackingIdAllocated = ((iTrackingIdAllocated + 1) % 65536);

			if(iFingerId == 0)
			{
				WriteEvent(iPointerFD, EV_KEY, BTN_TOUCH, 1);
				WriteEvent(iPointerFD, EV_SYN, SYN_REPORT, 0);
			}

			WriteEvent(iPointerFD, EV_ABS, ABS_MT_TRACKING_ID, iTrackingId[iFingerId]);
			WriteEvent(iPointerFD, EV_ABS, ABS_MT_POSITION_X, iX);
			WriteEvent(iPointerFD, EV_ABS, ABS_MT_POSITION_Y, iY);
			WriteEvent(iPointerFD, EV_ABS, ABS_MT_PRESSURE, 0x50);//添加触摸的压力系数，否则在手机重启的时候，可能会出现漏点的情况
			WriteEvent(iPointerFD, EV_SYN, SYN_REPORT, 0);
			break;
		case 1:		//touch_move
			WriteEvent(iPointerFD, EV_ABS, ABS_MT_SLOT, iFingerId);
			WriteEvent(iPointerFD, EV_ABS, ABS_MT_TRACKING_ID, iTrackingId[iFingerId]);
			WriteEvent(iPointerFD, EV_ABS, ABS_MT_POSITION_X, iX);
			WriteEvent(iPointerFD, EV_ABS, ABS_MT_POSITION_Y, iY);
			WriteEvent(iPointerFD, EV_ABS, ABS_MT_PRESSURE, 0x50);
			WriteEvent(iPointerFD, EV_SYN, SYN_REPORT, 0);
			break;
		case 2:		//touch_up
			WriteEvent(iPointerFD, EV_ABS, ABS_MT_SLOT, iFingerId);
			iTrackingId[iFingerId] = 0;
			WriteEvent(iPointerFD, EV_ABS, ABS_MT_TRACKING_ID, -1);
			WriteEvent(iPointerFD, EV_SYN, SYN_REPORT, 0);

			if(iFingerId == 0)
			{
				WriteEvent(iPointerFD, EV_KEY, BTN_TOUCH, 0);
				WriteEvent(iPointerFD, EV_SYN, SYN_REPORT, 0);
			}
			break;
		default:
			break;
		}
	}

	

	//delay
	if(iTime > 0)
	{
		if(iTime >= 1000)
			sleep(iTime / 1000);

		struct timeval delay_usec = { 0 };
		delay_usec.tv_usec = (iTime % 1000) * 1000;
		select(0, NULL, NULL, NULL, &delay_usec);
	}

	return 0;
}

void InputDevice::WritePointerInputTypeA(int iPointerFD, int iAction, int iX, int iY, int iFingerId)//Type A协议模式下录制回放
{
	LOGI("InputDevice::WritePointerInputTypeA %d %d finger=%d", iX, iY, iFingerId);
	STouchInfo::STouchInfoSlot pSlot;
	pSlot.Clear();
	pSlot.iTrackingId = iFingerId;
	pSlot.iX = iX;
	pSlot.iY = iY;

	switch(iAction)
	{
	case 0:		//touch_down
	case 1:		//touch_move
		AddPointToTouchInfoArray(m_MultiTouchPointInfo, &pSlot);
		break;
	case 2:		//touch_up
		DeleteTouchPointFromArray(m_MultiTouchPointInfo, &pSlot);
		break;
	default:
		break;
	}

	//发送事件
	int iNum = GetTouchPointNum(m_MultiTouchPointInfo);
	if (0 == iNum)
	{
		//表明是最后一个手指抬起来
		WriteEvent(iPointerFD, EV_KEY, BTN_TOUCH, 0);
		WriteEvent(iPointerFD, EV_SYN, SYN_MT_REPORT, 0);
		WriteEvent(iPointerFD, EV_SYN, SYN_REPORT, 0);
	}
	else
	{
		//TODO:区分手指第一次按下
		if (1 == iNum)//单手指
		{
			WriteEvent(iPointerFD, EV_KEY, BTN_TOUCH, 1);
		}
		
		for (int i = 0; i < MAX_SLOT_NUMBER; i++)
		{
			if (-1 != m_MultiTouchPointInfo[i].iTrackingId)
			{
				WriteEvent(iPointerFD, EV_ABS, ABS_MT_TRACKING_ID, m_MultiTouchPointInfo[i].iTrackingId);
				WriteEvent(iPointerFD, EV_ABS, ABS_MT_POSITION_X, m_MultiTouchPointInfo[i].iX);
				WriteEvent(iPointerFD, EV_ABS, ABS_MT_POSITION_Y, m_MultiTouchPointInfo[i].iY);
				WriteEvent(iPointerFD, EV_SYN, SYN_MT_REPORT, 0);
			}
		}

		WriteEvent(iPointerFD, EV_SYN, SYN_REPORT, 0);
	}

}

int InputDevice::WriteEvent(int fd, int iType, int iCode, int iValue)
{
	struct input_event event;
	gettimeofday(&(event.time),NULL);
	event.type = iType;
	event.code = iCode;
	event.value = iValue;
	LOGI("WriteEvent %d %d %d", iType, iCode, iValue);
	return write(fd, &event, sizeof(event));
}

int InputDevice::Cancel()
{
	write(m_ThreadEventPipe[1], &m_iKeyboardFDNumber, sizeof(m_iKeyboardFDNumber));	//随便写一个值到pipe，让等待的线程退出
}


/************************************************************************/
/*  Type A协议模式下的方法                                                             
/************************************************************************/
void InputDevice::AddPointToTouchInfoArray(STouchPointInfo TouchInfoArray[], STouchInfo::STouchInfoSlot *pSlot)//将触点的信息添加到多点触控的数组中
{
	if (-1 == pSlot->iTrackingId)//-1表示该点是最后一个手指抬起，故直接返回
	{
		return;
	}

	//首先查看该id是否已经在数组中有记录了
	for (int i = 0; i < MAX_SLOT_NUMBER; i++)
	{
		if (pSlot->iTrackingId == TouchInfoArray[i].iTrackingId)
		{
			TouchInfoArray[i].iTrackingId = pSlot->iTrackingId;
			TouchInfoArray[i].iX = pSlot->iX;
			TouchInfoArray[i].iY = pSlot->iY;
			return;
		}
	}

	//没有找到，则将其放在第一个空闲的位置中
	for (int i = 0; i < MAX_SLOT_NUMBER; i++)
	{
		if (-1 == TouchInfoArray[i].iTrackingId)
		{
			TouchInfoArray[i].iTrackingId = pSlot->iTrackingId;
			TouchInfoArray[i].iX = pSlot->iX;
			TouchInfoArray[i].iY = pSlot->iY;
			return;
		}
	}

}

void InputDevice::CopyTouchInfoArray(STouchPointInfo TouchInfoArrayFrom[], STouchPointInfo TouchInfoArrayTo[])//复制多点触控数组
{
	ClearTouchInfoArray(TouchInfoArrayTo);
	for (int i = 0; i < MAX_SLOT_NUMBER; i++)
	{
		if (-1 != TouchInfoArrayFrom[i].iTrackingId)
		{
			TouchInfoArrayTo[i].iTrackingId = TouchInfoArrayFrom[i].iTrackingId;
			TouchInfoArrayTo[i].iX = TouchInfoArrayFrom[i].iX;
			TouchInfoArrayTo[i].iY = TouchInfoArrayFrom[i].iY;
		}
	}
	ClearTouchInfoArray(TouchInfoArrayFrom);
}

bool InputDevice::IsTouchPointMove(STouchPointInfo TouchInfoArray[], STouchInfo::STouchInfoSlot *pSlot)//判断该触点是否有发生移动
{
	if (-1 == pSlot->iTrackingId)//-1表示该点是最后一个手指抬起，故直接返回false
	{
		return false;
	}

	for (int i = 0; i < MAX_SLOT_NUMBER; i++)
	{
		if ((pSlot->iTrackingId == TouchInfoArray[i].iTrackingId)
			&&(pSlot->iX == TouchInfoArray[i].iX)
			&&(pSlot->iY == TouchInfoArray[i].iY))
		{
			return false;
		}
	}
	return true;
}

void InputDevice::ClearTouchInfoArray(STouchPointInfo TouchInfoArray[])//清空多点触控的数组
{
	for (int i = 0; i < MAX_SLOT_NUMBER; i++)
	{
		TouchInfoArray[i].Clear();
	}
}

int InputDevice::GetTouchUpPoint(STouchPointInfo TouchInfoArrayCur[], STouchPointInfo TouchInfoArrayLatest[])//获取手指抬起的点，若没有手指抬起，则返回-1
{
	//在TouchInfoArrayLatest中但不在TouchInfoArrayCur中的触点，则认为是抬起的手指
	int iId = -1;
	int iRetVal = -1;
	bool bIsExist = false;
	for (int i = 0; i < MAX_SLOT_NUMBER; i++)
	{
		bIsExist = false;
		iId = TouchInfoArrayLatest[i].iTrackingId;
		//查找该id是否在TouchInfoArrayCur中
		for (int j = 0; j < MAX_SLOT_NUMBER; j++)
		{
			if (iId == TouchInfoArrayCur[j].iTrackingId)
			{
				bIsExist = true;
				break;
			}
		}

		if (!bIsExist)
		{
			iRetVal = iId;
			break;
		}
	}

	return iRetVal;
}

void InputDevice::DeleteTouchPointFromArray(STouchPointInfo TouchInfoArray[], STouchInfo::STouchInfoSlot *pSlot)//将触点的信息从多点触控的数组中删除
{
	if (-1 == pSlot->iTrackingId)//-1表示该点是最后一个手指抬起，故直接返回false
	{
		return;
	}

	for (int i = 0; i < MAX_SLOT_NUMBER; i++)
	{
		if (pSlot->iTrackingId == TouchInfoArray[i].iTrackingId)
		{
			TouchInfoArray[i].Clear();
		}
	}
}

int InputDevice::GetTouchPointNum(STouchPointInfo TouchInfoArray[])	//获取当前按下的手指的数目
{
	int iNum = 0;
	for (int i = 0; i < MAX_SLOT_NUMBER; i++)
	{
		if (-1 != TouchInfoArray[i].iTrackingId)
		{
			iNum++;
		}
	}
	return iNum;
}

void InputDevice::UnitTest()
{
	InputDevice::GetInstance().ReadKeyboard(true);
}

void InputDevice::SetProtoType(int iProtoType)
{
	m_ProtoType = iProtoType;
	LOGI("SetProtoType m_ProtoType=%d", m_ProtoType);
}

void InputDevice::WriteInitData(int iX, int iY)
{
	auto iPointerFD = m_pAllInputFD[1 + m_iKeyboardFDNumber].fd;

	WriteEvent(iPointerFD, EV_SYN, SYN_MT_REPORT, 0);
	WriteEvent(iPointerFD, EV_SYN, SYN_REPORT, 0);

	WriteEvent(iPointerFD, EV_ABS, ABS_MT_POSITION_X, iX);
	WriteEvent(iPointerFD, EV_ABS, ABS_MT_POSITION_Y, iY);
	WriteEvent(iPointerFD, EV_SYN, SYN_MT_REPORT, 0);
	WriteEvent(iPointerFD, EV_SYN, SYN_REPORT, 0);

}