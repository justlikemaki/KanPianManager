<%@ page language="java"  pageEncoding="UTF-8"%>
<%@include file="taglib.jsp"%>
<link rel="stylesheet" href="css/setting.css">
<div class="setting  mdl-shadow--3dp">
		<h3>设置</h3>
		<div class="settingone">
			<ul class="addeditlist">
				<li><div class="fl dl">类型:</div><div class="fr dr pr">
					<select id="settype">
						<option value="censored">有码</option>
						<option value="uncensored">无码</option>
						<option value="westpron">欧美</option>
					</select>
					<div class="selectclip"></div>
				</div></li>
				<li><div class="fl dl">页码:</div><div class="fr dr">
					<div class="inputdiv mdl-textfield mdl-js-textfield is-upgraded is-dirty" data-upgraded=",MaterialTextfield">
						<input class="mdl-textfield__input formparam" id="fild" placeholder="页码数用','隔开,连续的用'--'" type="text"  title="页码数用','隔开,连续的用'--'"/>
						<label class="mdl-textfield__label" for="fild"></label>
					</div>
				</div></li>
				<li><div class="fl dl">关键字:</div><div class="fr dr">
					<div class="inputdiv mdl-textfield mdl-js-textfield is-upgraded is-dirty" data-upgraded=",MaterialTextfield">
						<input class="mdl-textfield__input formparam" id="fhkey" placeholder="番号关键字或#网站路径" type="text"/>
						<label class="mdl-textfield__label" for="fhkey"></label>
					</div>
				</div></li>
				<li><div class="fl dl">线程数:</div><div class="fr dr pr">
					<select id="thdnum">
						<option value="1">开启1个线程</option>
						<option value="5">开启5个线程</option>
						<option value="10">开启10个线程</option>
						<option value="20">开启20个线程</option>
					</select>
					<div class="selectclip"></div>
				</div></li>
			</ul>
			<button id="fildpage" class="mdl-button mdl-js-button mdl-button--raised mdl-button--colored">
				获取输入页码数据
			</button>
			<button id="addpage" class="mdl-button mdl-js-button mdl-button--raised mdl-button--colored">
				加入到自动获取列表(errlist)
			</button>
		</div>
		<hr>
		<button id="updatecache" class="mdl-button mdl-js-button mdl-button--raised mdl-button--colored">
			刷新缓存
		</button>
		<button id="readprop" class="mdl-button mdl-js-button mdl-button--raised mdl-button--colored">
			读取并刷新配置
		</button>
		<button id="readuuid" class="mdl-button mdl-js-button mdl-button--raised mdl-button--colored">
			读取并刷新设备号
		</button>
		<hr>
		<button id="tobase64" class="mdl-button mdl-js-button mdl-button--raised mdl-button--colored">
			CDN图片转BASE64
		</button>
		<button id="classtobase64" class="mdl-button mdl-js-button mdl-button--raised mdl-button--colored">
			经典资源转BASE64
		</button>
		<button id="westporntobase64" class="mdl-button mdl-js-button mdl-button--raised mdl-button--colored">
			欧美片转BASE64
		</button>
		<hr>
		<div class="settingtwo">
			<ul class="addeditlist">
				<li><div class="fl dl">输入:</div><div class="fr dr">
					<div class="inputdiv mdl-textfield mdl-js-textfield is-upgraded is-dirty" data-upgraded=",MaterialTextfield">
						<input class="mdl-textfield__input formparam" type="text" id="inpath" placeholder="输入文件夹路径"/>
						<label class="mdl-textfield__label" for="fild"></label>
					</div>
				</div></li>
				<li><div class="fl dl">输出:</div><div class="fr dr">
					<div class="inputdiv mdl-textfield mdl-js-textfield is-upgraded is-dirty" data-upgraded=",MaterialTextfield">
						<input class="mdl-textfield__input formparam" type="text" id="outpath" placeholder="输出文件夹路径"/>
						<label class="mdl-textfield__label" for="fild"></label>
					</div>
				</div></li>
			</ul>
			<button id="doWriteExl" class="mdl-button mdl-js-button mdl-button--raised mdl-button--colored">
				生成exl
			</button>
			<button id="doExportFiles" class="mdl-button mdl-js-button mdl-button--raised mdl-button--colored">
				提取所有
			</button>
			<button id="doExportJpg" class="mdl-button mdl-js-button mdl-button--raised mdl-button--colored">
				提取图片
			</button>
		</div>
</div>
<script type="text/javascript" src="js/setting.js"></script>
