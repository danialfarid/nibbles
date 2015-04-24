Oo.future(function () {
	var nibbles = M.nibbles = {
	    worm: {},
	    worms: [],
	    conf: {size: {}},
	    ui: {},
		joined: {size: {}}
	};

	if (localStorage) {
		nibbles.joinId = localStorage.getItem('joinId') || '';
		nibbles.worm.name = localStorage.getItem('name') || 'Player 1';
		nibbles.worm.color = localStorage.getItem('color') || 'red';
		nibbles.conf.id = localStorage.getItem('createId') || parseInt(Date.now() / 1000).toString();
		nibbles.conf.speed = localStorage.getItem('speed') || 50;
		nibbles.conf.lives = localStorage.getItem('lives') || 10;
		nibbles.conf.size.x = localStorage.getItem('sizex') || 80;
		nibbles.conf.size.y = localStorage.getItem('sizey') || 50;
		nibbles.ui.pixx = localStorage.getItem('pixx') || 12;
		nibbles.ui.pixy = localStorage.getItem('pixy') || 12;
		nibbles.ui.playgroundColors = localStorage.getItem('playgroundColors') ||
													'white, orange, black, white';
	}

	var playground;
	var baseApi = '/_ah/api/nibbles/v1/';

	Oo.http().get(baseApi + 'u').send().after(function(data) {
	    if (data != null && data.str) {
	        if (confirm("Login to google")) {
	            window.location.href = data.str;
	        }
	    }
	});

	nibbles.createGame = function() {
		Oo.http().post(baseApi + 'g/' + nibbles.conf.id).json(nibbles.conf).after(function(data) {
	        nibbles.joinGame(nibbles.conf.id);
	    }, function(status, data) {
	        if (status == 400) {
	            alert(data.error.message);
	        }
	    });
	};

	var gameInfoInterval;
	nibbles.joinGame = function(id) {
	    Oo.http().put(baseApi + 'g/' + id).send().after(function(data) {
            openChannel(data.token);
			gameInfoInterval = setInterval(function () {
				Oo.http().get(baseApi + 'g/' + id).send().after(function(data) {
					data = data.data;
					if (nibbles.worms.length != data.worms.length) {
						nibbles.worms = data.worms;
					}
					if (nibbles.joined.id == null) {
						nibbles.joined = data.config;
					}
				});
			}, 2000);
        }, function(status, data) {
            if (status == 400) {
                alert('Game not found!' + data);
            }
        });
	};

	var openChannel = function(token) {
		var channel = new goog.appengine.Channel(token);
		var handler = {
			'onerror': function() {},
			'onclose': function() {},
			'onopen': function() {},
			'onmessage': onMessage
		};
		var socket = channel.open(handler);
		socket.onmessage = onMessage;
	};

	function onMessage(msg) {
	    if (msg.type === 'joined') {
	        nibbles.worms[msg.data.n] = msg.data;
	    }
	    console.log(msg);
	}

	document.onkeyup = function(e) {
		switch (e.which) {
		case 13:
		case 32:
//		    api('post', 'p/' + gameId + '/m/' + , )
			if (playground == null || (playground != null && playground.interval == null)) {
		        api('post', 'p/' + gameId + '/ready');
				confWidth = document.getElementById('conf').offsetWidth + 'px';
				confHeight = document.getElementById('conf').offsetHeight + 'px';
				document.getElementById('scores').innerHTML = '';
				for (var i = 0; i < conf.n; i++) {
					conf.names[i] = conf.names[i] || ('Player ' + (i + 1));
					document.getElementById('scores').innerHTML += '<div class="score p' + (i + 1) + '" style="width: ' + (100 / conf.n) + 
						'%"><b>&nbsp;&nbsp;' + 
						conf.names[i] + '</b>&nbsp;&nbsp;<span class="heart">&hearts;</span><span id="p' + (i + 1) + 'Lives">' + 
						conf.lives + '</span>&nbsp;&nbsp;<span class="money">$</span><span id="p' + (i + 1) + 'Score">0</span></div>' 
				}
				playground = new Playground().init(conf, document.getElementById('playground'));
				document.getElementById('scores').style.width = document.getElementById('playground').style.width;
				
				playground.gameOverCallback = function() {
					document.getElementById('game-result').innerHTML = playground.gameOver;
					document.getElementById('conf').show();
				};
				playground.livesCallback = function(n, v) {
					document.getElementById('p' + n + 'Lives').innerHTML = v;
				};
				playground.scoreCallback = function(n, v) {
					document.getElementById('p' + n + 'Score').innerHTML = v;
				};
				document.getElementById('conf').hide();
				
				if (localStorage) {
					localStorage.conf = [document.getElementById('n').value, document.getElementById('names').value, 
					                     document.getElementById('colors').value,
					                     document.getElementById('speed').value, document.getElementById('lives').value, 
					                     document.getElementById('sizex').value, document.getElementById('sizey').value, 
					                     document.getElementById('pixx').value, document.getElementById('pixy').value,
					                     document.getElementById('playgroundColors').value].join("`"); 
				}
				playground.isPaused = false;
			}
			break;
		case 27:
			if (playground != null) {
				if (playground.interval) {
					playground.pause();
					document.getElementById('conf').show();
				} else {
					if (!playground.gameOver) {
						playground.isPaused = false;
						document.getElementById('conf').hide();
					}
				}
			}
			break;
		}
		
	}
	
	document.getElementById('conf').show = function() {
		document.getElementById('conf').style.visibility = 'visible';
		document.getElementById('conf').style.height = 'auto';		
	}
	document.getElementById('conf').hide = function() {
		document.getElementById('conf').style.visibility = 'hidden';
		document.getElementById('conf').style.height = 0;
	}
	
	var getConf = function() {
		return {
			n:	parseInt(document.getElementById('n').value),
			speed: parseInt(document.getElementById('speed').value),
			lives: parseInt(document.getElementById('lives').value),
			names: document.getElementById('names').value.split(/ *, */g),
			colors: document.getElementById('colors').value.split(/ *, */g),
			size: {
				x: parseInt(document.getElementById('sizex').value),
				y: parseInt(document.getElementById('sizey').value)
			},
			pixel: {
				x: parseInt(document.getElementById('pixx').value),
				y: parseInt(document.getElementById('pixy').value)
			},
			playgroundColor: document.getElementById('playgroundColors').value.split(/ *, */g)[0],
			wallsColor: document.getElementById('playgroundColors').value.split(/ *, */g)[1],
			foodColor: document.getElementById('playgroundColors').value.split(/ *, */g)[2],
			foodBackground: document.getElementById('playgroundColors').value.split(/ *, */g)[3]
		}
	};

	var Playground = function() {
		return {
			isPaused: false,
			matrix: [],
			get: function(x, y) {
				return this.matrix[x][y].value;
			},
			set: function(x, y, v) {
				if (x == undefined) {
					console.log(x, y);
				}
				this.matrix[x][y].value = v;
				this.draw(x, y, v);
			},
			worms: [],
			broadcast: function(message) {
				this.pause();
				this.gameOver = message;
				this.gameOverCallback();
			},
			gameOver: false,
			gameOverCallback: function() {},
			livesCallback: function() {},
			scoreCallback: function() {},
			randomLoc: function() {
				var x, y;
				do {
					x = 3 + parseInt(Math.random() * (this.matrix.length - 6));
					y = 3 + parseInt(Math.random() * (this.matrix[0].length - 6));
				} while(this.get(x,y) != null);
				return {x: x, y:y};
			},
			randomDir: function() {
				switch (parseInt(Math.random() * 4)) {
				case 0: return 'l';
				case 1: return 'u';
				case 2: return 'r';
				case 3: return 'd';
				}
			},
			food: {
				loc: {x: 1, y: 1}, 
				food: 0, 
				isEaten: true 
			},
			eat: function(food) {
				food.isEaten = true; 
				this.set(food.loc.x, food.loc.y, null);
				return food.food;
			},
			tick: function() {
				if (this.food.isEaten) {
					this.food.isEaten = false;
					this.food.food++;
					var loc = this.randomLoc();
					this.food.loc = loc;
					this.set(loc.x, loc.y, this.food);
				}
				for (var i = 0; i < this.worms.length; i++) {
					var worm = this.worms[i];
					if (!worm.alive) {
						this.die(worm);
					} else {
						worm.step();
					}
				}
			},
			die: function(worm) {
				var n = this.worms.length;
				/* if (punish && this.food.food > 1) {
					this.food.food--;
					this.draw(this.food.loc.x, this.food.loc.y, this.get(this.food.loc.x, this.food.loc.y));
				} */
				this.worms.splice(this.worms.indexOf(worm), 1);
				if (worm.lives > 0) {
					worm.lives--;
					this.livesCallback(worm.number, worm.lives);
					this.worms.push(worm.init());					
				} else {						
					if (n == 1) {
						this.broadcast('Level: ' + this.food.food + ' Score: ' + worm.score);
					} else if (this.worms.length == 1) {
						this.broadcast(this.worms[0].name + ' Wins! level: ' + 
								this.food.food + " score: " + this.worms[0].score);
					}
				}
			},
			init: function(config, playgroundElement) {		
				playgroundElement.innerHTML = '';
				for (var y = 0; y < config.size.y; y++) {
					var tr = document.createElement('tr');
					playgroundElement.appendChild(tr);
					for (var x = 0; x < config.size.x; x++) {
						if (!this.matrix[x]) {
							this.matrix[x] = [];
						}
						var td = document.createElement('td');
						tr.appendChild(td);
						this.matrix[x][y] = {value: null, elem: td};
					}
				}

				for (var i = 0; i < config.n; i++) {
					var worm = new Worm(i + 1, config.names[i], config.lives, this).init(config.size);
					var _this = this;
					worm.scoreCallback = function(n, v) {
						_this.scoreCallback(n, v);
					}
					this.worms.push(worm);
				}
				for (var i = 0; i < config.size.x; i++) {					
					this.set(i, 0, this.wall);
					this.set(i, config.size.y - 1, this.wall);
				}
				for (var i = 0; i < config.size.y; i++) {
					this.set(0, i, this.wall);
					this.set(config.size.x - 1, i, this.wall);
				}
				
				playgroundElement.style.width = config.size.x * config.pixel.x + 'px';
				playgroundElement.style.height = config.size.y * config.pixel.y + 'px';
				playgroundElement.style.lineHeight = config.pixel.y + 'px';
				var css = '#playground td {width:' + config.pixel.x + 'px;height:' + config.pixel.y + 'px;}' + 
					'#playground {background:' + (config.playgroundColor || 'white') + ';} #playground .wall {background: ' + (config.wallsColor || 'orange') + ';}' +
					'#conf {box-shadow: 0px 0px 10px 1px ' + (config.wallsColor || 'orange') + ' !important;' +
					'top: ' + (config.pixel.x * 2) + 'px !important; left: ' + (config.pixel.y * 2) + 'px !important;}' +
					'#playground .food{color: ' + (config.foodColor || 'black') + '; background: ' + (config.foodBackground || '#F9F9F9') + ';}' +
					'#playground td.p1, #score p1 {background: ' + (config.colors[0] || 'red') + 
					';} #playground td.p2, #score p2 {background: ' + (config.colors[1] || 'blue') +
					';} #playground td.p3, #score p3 {background: ' + (config.colors[0] || 'yellow') + 
					';}#playground td.p4, #score p4 {background: ' + (config.colors[1] || 'green') + ';}';
			    var style = document.getElementById('style');
				if (style.styleSheet){
					style.styleSheet.cssText = css;
				} else {
					while (style.hasChildNodes()) {
						style.removeChild(style.lastChild);
					}
					style.appendChild(document.createTextNode(css));
				}
				
				this.speed = config.speed;
				
				this.tick();
				var _this = this;
								
				document.onkeydown = function(e) {
					if (_this.interval == null) {
						if (!_this.gameOver && !_this.isPaused && e.which != 27) {
							_this.resume();							
						}
					}
					if (_this.interval != null) {
						for (var i = 0; i < _this.worms.length; i++) {
							var worm = _this.worms[i];
							code = e.which + worm.number * 1000;
							var map = _this.keyMap[code];
							if (map != null) {
								if (worm.head.d != map[1]) worm.turn(map[0]);
								e.preventDefault();
							}
						}
						if (e.which > 31 && e.which < 35) {
							e.preventDefault();
						}
					}
				}		
				
				return this;
			}, 
			speed: 50,
			keyMap: {
				1037: ['l', 'r'], 1038: ['u', 'd'], 1039: ['r', 'l'], 1040: ['d', 'u'], 
				2065: ['l', 'r'], 2087: ['u', 'd'], 2068: ['r', 'l'], 2083: ['d', 'u'], 
				3071: ['l', 'r'], 3089: ['u', 'd'], 3074: ['r', 'l'], 3072: ['d', 'u'], 
				4076: ['l', 'r'], 4080: ['u', 'd'], 4222: ['r', 'l'], 4186: ['d', 'u'] 
			},
			resume: function() {
				this.isPaused = false;
				var _this = this;
				this.interval = setInterval(function() {_this.tick()}, 1000 / _this.speed);
			},
			pause: function() {
				this.isPaused = true;
				clearInterval(this.interval);
				this.interval = null;
			},
			interval: null,
			wall: {
				isWall: true
			},
			draw: function(x, y, v) {
				if (!v) {
					this.matrix[x][y].elem.removeAttribute('class');
					while (this.matrix[x][y].elem.hasChildNodes()) {
						this.matrix[x][y].elem.removeChild(this.matrix[x][y].elem.lastChild);
					}
				} else if (v.number) {
					this.matrix[x][y].elem.setAttribute('class', 'p' + v.number);
				} else if (v.isWall) {
					this.matrix[x][y].elem.setAttribute('class', 'wall');						
				} else if (v.food != null) {
					var foodEl = document.createElement('span');
					foodEl.setAttribute('class', +v.food >= 10 ? 'food ge10' : 'food');
					foodEl.innerHTML = v.food;
					this.matrix[x][y].elem.appendChild(foodEl);
				}
			}
		 }
	};
	
	var Worm = function(n, name, lives, playground) {		
		return {
			number: n,
			head: {},
			tail: {},
			turns: [],			
			turn: function(d) {
				if (this.turns.length === 0 || this.turns[this.turns.length - 1] !== d) {
					this.turns.push(d);
				}
			},
			init: function(size) {
				if (this.head.x == null) {
					switch (this.number) {
					case 1: this.head = {x: parseInt(size.x / 4), y: parseInt(size.y / 2), d: 'r'};
					break
					case 2: this.head = {x: parseInt(3 * size.x / 4), y: parseInt(size.y / 2), d: 'l'};
					break
					case 3: this.head = {x: parseInt(size.x / 2), y: parseInt(size.y / 4), d: 'd'};
					break
					case 4: this.head = {x: parseInt(size.x / 2), y: parseInt(3 * size.y / 4), d: 'u'};
					break
					}
				} else {
					var loc = playground.randomLoc();
					this.head = {x: loc.x, y: loc.y, d: playground.randomDir()};
				}
				playground.set(this.head.x, this.head.y, this);
				this.tail = {next: this.head};
				this.length = 3;
				this.grow = 1;
				this.alive = true;				
				this.step();
				this.step();
				return this;
			},
			alive: true,
			name: name,
			grow: 1,
			lives: lives,
			score: 0,
			length: 3,
			step: function() {
				var newHead;
				switch(this.turns.pop() || this.head.d) {
				case 'r': newHead = {x: this.head.x + 1, y: this.head.y, d: 'r'};
				break;
				case 'l': newHead = {x: this.head.x - 1, y: this.head.y, d: 'l'};
				break;
				case 'd': newHead = {x: this.head.x, y: this.head.y + 1, d: 'd'};
				break;
				case 'u': newHead = {x: this.head.x, y: this.head.y - 1, d: 'u'};
				break;
				}
				this.head.next = newHead;
				this.head = newHead;
				this.eat(this.head.x, this.head.y);
				if (this.alive) {
					playground.set(this.head.x, this.head.y, this);
					if (this.grow > 0) {
						this.grow--;
					} else {
						if (this.tail.x != null) {
							playground.set(this.tail.x, this.tail.y, null);
						}
						this.tail = this.tail.next;
					}
				}
			},
			eat: function(x, y) {
				var v = playground.get(x, y);
				if (!v) {
				} else if (v.isWall || v.number) {
					if (v.number) {
						if (v.head.x == x && v.head.y == y) {
							v.die();
						} else {
							if (v.alive) {
								v.digest(this.length);
							}
						}
					}
					this.die();
				} else if (v.food) {
					var f = playground.eat(v)
					this.digest(f);
					this.length += f; 
				} else if (v.life) {
					this.lives += v.lives();
				}
			},
			digest: function(v) {
				this.grow += v;
				this.score += v;
				this.scoreCallback(this.number, this.score);
			},
			scoreCallback : function(){},
			die: function() { 
				this.alive = false;
				this.score = Math.max(this.score - 10, 0);
				this.scoreCallback(this.number, this.score);
				while (this.tail != this.head) {
					if (this.tail.x != null) {
						playground.set(this.tail.x, this.tail.y, null);
					}
					this.tail = this.tail.next;
				}
				if (playground.get(this.head.x, this.head.y) === this) {
					playground.set(this.head.x, this.head.y, null);
				}
			}
		}
	};
});
