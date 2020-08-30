/**
 * student.html
 * 
 */

var vm = new Vue({
	el: "#app",
	data: {
		username: null,
		title: "DI网盘",
		tree: [{
			text: "全部文件",
		}, {
			text: "图片",
		}, {
			text: "文档",
		}, {
			text: "其他",
		}],
		// 表格头
		tabletitle: ["选择", "类型", "文件名", "大小", "修改日期"],
		// 当前显示界面的标量
		menu: 0,
		// 根路径
		rootPath: "",
		// 当前路径
		path: "",
		// 显示在界面上的路径文本
		pathText: "",
		// 当前页面文件数
		fileCount: 0,
		// 文件列表内容
		listContent: [],
		// 是否在根路径
		isRoot: true,
		// 新建文件夹的名称
		newDir: "",
		// 搜索的关键字
		key: "",
		// 被选择的文件列表
		selected: [],
	},
	methods: {
		// 验证用户是否已登录
		validation() {
			axios.post("/validation").then(function(res) {
				if (res.data.code == -1) {
					location.href = "index.html"
					return
				}
				// 保存当前用户名
				vm.username = res.data.obj.name
				// 用户根目录
				vm.rootPath = res.data.obj.path
				// 用户当前目录
				vm.pathText = res.data.obj.cpath
				// 用户目录
				vm.path = res.data.obj.cpath
				if (vm.pathText != "")
					vm.isRoot = false
				else
					vm.isRoot = true
				vm.getListInfo()
			})
		},
		// 创建目录
		showTree() {
			$('#tree').treeview({
				data: this.tree,
				levels: 0,
				showBorder: true,
				onhoverColor: "#c9f9ff",
				highlightSelected: true,
				collapseIcon: "glyphicon glyphicon-folder-open",
				expandIcon: "glyphicon glyphicon-folder-close",
				emptyIcon: "glyphicon glyphicon-list-alt",
				backColor: "#aae2f9",
				searchResultColor: "#FF9900"
			})
			this.createClick()
		},
		// 创造Tree的点击事件监听
		createClick() {
			$('#tree').on('nodeSelected', function(event, data) {
				switch (data.nodeId) {
					case 0: // 全部文件
						vm.isRoot = false
						vm.backToAll();
						break
					case 1: // 图片文件
						vm.isRoot = false
						vm.getFilter(1);
						break
					case 2: // 文档文件
						vm.isRoot = false
						vm.getFilter(2);
						break
					case 3: // 其他文件
						vm.isRoot = false
						vm.getFilter(3);
						break
					default:
						break
				}
			})
		},
		// 获取文件树内容
		getListInfo() {
			axios({
				url: "/getList",
				method: "post",
				params: {
					path: this.path
				}
			}).then(function(res) {
				var res = res.data
				if (res.code == -1) {
					vm.listContent = []
					vm.fileCount = 0
				} else {
					vm.listContent = res.obj.list
					vm.fileCount = res.obj.size
					vm.pathText = res.msg
					if (vm.pathText == "")
						vm.isRoot = true;
				}
			})
		},
		// 进入子目录
		nextDir(name) {
			console.log(name)
			// 清空已选列表
			this.selected = []
			// 切换到文件展示列表
			this.menu = 0
			// 更新路径
			this.path = name
			this.isRoot = false
			this.getListInfo()
		},
		// 回到上一级目录
		previous() {
			this.path = "@#$^"
			if (this.path == "")
				this.backToAll()
			else
				this.getListInfo()
		},
		// 回到根目录
		backToAll() {
			if (this.isRoot)
				return
			this.menu = 0
			// 清空当前路径
			this.path = ""
			this.isRoot = true
			this.getListInfo()
		},
		// 新建文件夹
		mkDir() {
			axios({
				url: "/mkDir",
				method: "post",
				params: {
					newDir: this.newDir
				}
			}).then(function(res) {
				var res = res.data
				vm.listContent = res.obj.list
				vm.fileCount = res.obj.size
				if (res.code == -1) {
					alert("新建文件失败")
				} else {
					alert("新建文件成功")
					vm.newDir = ""
				}
			})
		},
		// 搜索
		searchOp() {
			if (this.key == "")
				return
			// 清空已选列表
			this.selected = []
			// 显示搜索结果列表
			this.menu = 1
			// 清空当前路径
			this.pathText = ""

			this.isRoot = false
			axios({
				url: "/searchOp",
				method: "post",
				params: {
					key: this.key
				}
			}).then(function(res) {
				var res = res.data
				if (res.code == -1) {
					vm.listContent = []
					vm.fileCount = 0
				} else {
					vm.listContent = res.obj.list
					vm.fileCount = res.obj.size
				}
			})
		},
		// 过滤显示
		getFilter(type) {
			// 清空已选列表
			this.selected = []
			// 显示搜索结果列表
			this.menu = 1
			// 清空当前路径
			this.path = ""
			this.isRoot = true
			axios({
				url: "/filter",
				method: "post",
				params: {
					type: type
				}
			}).then(function(res) {
				var res = res.data
				if (res.code == -1) {
					vm.listContent = []
					vm.fileCount = 0
				} else {
					vm.listContent = res.obj.list
					vm.fileCount = res.obj.size
				}
			})
		},
		// 上传文件
		uploadFile(event) {
			var formData = new FormData() // 声明一个FormData对象
			var formData = new window.FormData()
			var file = event.target.files[0];
			formData.append('uploadFile', file)
			var options = { // 设置axios的参数
				url: '/uploadFile',
				data: formData,
				method: 'post',
				headers: {
					'Content-Type': 'multipart/form-data'
				}
			}
			axios(options).then((res) => {
				var res = res.data
				vm.listContent = res.obj.list
				vm.fileCount = res.obj.size
				if (res.code == 0) {
					alert("文件上传成功")
				} else {
					alert("文件上传失败")
				}
			})
		},
		// 下载文件
		downLoad() {
			if (this.selected.length == 0)
				return
			location.href = "/downLoad?files=" + JSON.stringify(this.selected)
			this.selected == []
		},
		// 删除文件
		deleteOp() {
			if (this.selected.length == 0)
				return
			if (!confirm("确认删除吗?")) {
				return
			}
			axios({
				url: "/delete",
				method: "post",
				params: {
					files: JSON.stringify(this.selected)
				}
			}).then(function(res) {
				var res = res.data
				vm.listContent = res.obj.list
				vm.fileCount = res.obj.size
				if (res.code == -1) {
					alert("删除文件失败")
				} else {
					alert("删除文件成功")
					vm.selected = ""
				}
			})
		},
		exit() {
			axios.post("/exit").then(response => (location.href = "index.html"))
		},
	},
	mounted() {
		this.validation()
		this.showTree()
	}

})
