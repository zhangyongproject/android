package com.buxiubianfu.IME.command;

import java.util.Map;

import com.buxiubianfu.IME.command.Data.CommandData;



//指令接口
public interface ICommand {
	
			/**
			 * @param cmd 传入的指令
			 * @Intent 传递的参数
			 * @return 返回结果
			 */
			String Do(CommandData commandData);
}
