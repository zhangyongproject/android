#include <sys/poll.h>
#include <jni.h>

#define MAX_FD_NUMBER	8
#define MAX_SLOT_NUMBER	32
#define MAX_KEY_NUMBER 256

#define PROTO_TYPE_UNKNOW	-1
#define PROTO_TYPE_A		0
#define PROTO_TYPE_B		1

typedef struct STouchPointInfoS
{
	int iX;
	int iY;
	int iTrackingId;	//手指的ID编号, -1表示空闲

	void Clear()
	{
		iX = iY = -1;
		iTrackingId = -1;
	}
}STouchPointInfo;


class InputDevice
{
public:
	static void UnitTest();
	static InputDevice& GetInstance();
	static void DeleteInstance();

	int ReadInput(bool bGetKeyDownOnly, bool bIsIncludeTouchScreenDevice, int ReturnValues[]);		//对于键盘消息，只要返回值就可以了。对于触摸屏消息，还需要在ReturnValues里面放置更多的信息
	int ReadKeyboard(bool bGetKeyDownOnly);
	int WritePointerInput(int iAction, int iX, int iY, int iFingerId, int iTime);
	int Cancel();
	void SetProtoType(int iProtoType);
	void WriteInitData(int iX, int iY);
protected:
	InputDevice();
	~InputDevice();

	int AddDevice(int fd);
	void BuildScanCodeMapping();
	int WriteEvent(int fd, int iType, int iCode, int iValue);

	static bool containsNonZeroByte(const uint8_t* array, uint32_t startIndex, uint32_t endIndex);

private:
	struct 
	{	
		struct pollfd EventPipe;
		struct pollfd m_KeyboardPollFD[MAX_FD_NUMBER];
		struct pollfd m_TouchScreenPollFD[MAX_FD_NUMBER];
	} m_InputDevFD;

	struct STouchInfo
	{
		struct STouchInfoSlot
		{
			int iX;
			int iY;
			int iTrackingId;
			bool bIsModified;

			void Clear()
			{
				iX = iY = -1;
				iTrackingId = 0;
				bIsModified = false;
			}
		} m_Slot[MAX_SLOT_NUMBER];
		UINT m_nCurrentSlot;

		STouchPointInfo m_LatestTouchInfo[MAX_SLOT_NUMBER];
		STouchPointInfo m_CurTouchInfo[MAX_SLOT_NUMBER];

		STouchInfo() 
		{
			m_nCurrentSlot = 0;
			for(auto i = 0; i < MAX_SLOT_NUMBER; i++)
			{
				m_Slot[i].Clear();
				m_LatestTouchInfo[i].Clear();
				m_CurTouchInfo[i].Clear();
			}
		}
	} *m_pTouchInfo;

	struct pollfd *m_pAllInputFD;

	class Cleaner
	{
	public:
		Cleaner() {}; 
		~Cleaner() 
		{
			InputDevice::DeleteInstance();
		}
	}; 

	int m_iKeyboardFDNumber;
	int m_iTouchScreenFDNumber;
	int m_ThreadEventPipe[2];
	BYTE  m_KeyboardScanCodeMapping[MAX_KEY_NUMBER];

	static InputDevice* s_pInstance;
	int m_ProtoType;	//表示input驱动的协议类型，-1表示未知，0表示typeA; 1表示typeB
	STouchPointInfo m_MultiTouchPointInfo[MAX_SLOT_NUMBER];

private:
	void AddPointToTouchInfoArray(STouchPointInfo TouchInfoArray[], STouchInfo::STouchInfoSlot *pSlot);//将触点的信息添加到多点触控的数组中
	void CopyTouchInfoArray(STouchPointInfo TouchInfoArrayFrom[], STouchPointInfo TouchInfoArrayTo[]);//复制多点触控数组
	bool IsTouchPointMove(STouchPointInfo TouchInfoArray[], STouchInfo::STouchInfoSlot *pSlot);//判断该触点是否有发生移动
	void ClearTouchInfoArray(STouchPointInfo TouchInfoArray[]);//清空多点触控的数组
	int GetTouchUpPoint(STouchPointInfo TouchInfoArrayCur[], STouchPointInfo TouchInfoArrayLatest[]);//获取手指抬起的点，若没有手指抬起，则返回-1
	void DeleteTouchPointFromArray(STouchPointInfo TouchInfoArray[], STouchInfo::STouchInfoSlot *pSlot);//将触点的信息从多点触控的数组中删除
	int GetTouchPointNum(STouchPointInfo TouchInfoArray[]);	//获取当前按下的手指的数目

	void WritePointerInputTypeA(int iPointerFD, int iAction, int iX, int iY, int iFingerId);//Type A协议模式下录制回放
};