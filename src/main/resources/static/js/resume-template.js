/* global window */
(function (root) {
  'use strict';

  var helpers = Object.create(null);

  var escapeMap = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#x27;',
    '`': '&#x60;',
    '=': '&#x3D;'
  };
  var escapeRegex = /[&<>"'`=]/g;
  var escapePossible = /[&<>"'`=]/;

  function escapeHtml(value) {
    if (value && typeof value.toHTML === 'function') {
      return value.toHTML();
    }
    if (value == null) {
      return '';
    }
    if (!value) {
      return String(value);
    }
    var str = String(value);
    if (!escapePossible.test(str)) {
      return str;
    }
    return str.replace(escapeRegex, function (chr) {
      return escapeMap[chr];
    });
  }

  function hasOwn(obj, key) {
    return Object.prototype.hasOwnProperty.call(obj, key);
  }

  function resolvePath(path, context, rootContext) {
    if (path === 'this' || path === '.' || path === '') {
      return context;
    }
    var parts = path.split('.');
    var value;
    if (context != null && typeof context === 'object' && hasOwn(context, parts[0])) {
      value = context[parts[0]];
    } else if (rootContext != null && typeof rootContext === 'object' && hasOwn(rootContext, parts[0])) {
      value = rootContext[parts[0]];
    } else {
      return undefined;
    }
    for (var i = 1; i < parts.length; i++) {
      if (value == null) {
        return value;
      }
      value = value[parts[i]];
    }
    return value;
  }

  function splitParts(value) {
    return value.trim().split(/\s+/).filter(Boolean);
  }

  function parseExpression(expr) {
    var trimmed = expr.trim();
    if (!trimmed) {
      return null;
    }
    var parts = splitParts(trimmed);
    if (parts.length > 1) {
      return { type: 'helper', name: parts[0], args: parts.slice(1) };
    }
    var token = parts[0];
    if (token.charAt(0) === '(' && token.charAt(token.length - 1) === ')') {
      var inner = token.slice(1, -1).trim();
      if (!inner) {
        return null;
      }
      var innerParts = splitParts(inner);
      return { type: 'helper', name: innerParts[0], args: innerParts.slice(1) };
    }
    return { type: 'path', path: token };
  }

  function parseTemplate(template) {
    var rootNode = { type: 'root', children: [] };
    var stack = [rootNode];
    var index = 0;

    while (index < template.length) {
      var start = template.indexOf('{{', index);
      if (start === -1) {
        stack[stack.length - 1].children.push({
          type: 'text',
          value: template.slice(index)
        });
        break;
      }
      if (start > index) {
        stack[stack.length - 1].children.push({
          type: 'text',
          value: template.slice(index, start)
        });
      }
      var end = template.indexOf('}}', start + 2);
      if (end === -1) {
        stack[stack.length - 1].children.push({
          type: 'text',
          value: template.slice(start)
        });
        break;
      }
      var tag = template.slice(start + 2, end).trim();
      if (tag.indexOf('#each') === 0) {
        var eachExpr = tag.slice(5).trim();
        var eachNode = {
          type: 'each',
          expr: parseExpression(eachExpr),
          children: []
        };
        stack[stack.length - 1].children.push(eachNode);
        stack.push(eachNode);
      } else if (tag === '/each') {
        if (stack.length > 1) {
          stack.pop();
        }
      } else {
        stack[stack.length - 1].children.push({
          type: 'expr',
          expr: parseExpression(tag)
        });
      }
      index = end + 2;
    }

    return rootNode.children;
  }

  function resolveHelper(name, args, scope) {
    var helper = helpers[name];
    if (typeof helper !== 'function') {
      return undefined;
    }
    return helper.apply(scope.context, args);
  }

  function resolveArg(arg, scope) {
    if (arg === 'this') {
      return scope.context;
    }
    var value = resolvePath(arg, scope.context, scope.root);
    if (value !== undefined) {
      return value;
    }
    return resolveHelper(arg, [], scope);
  }

  function evaluateExpression(expr, scope) {
    if (!expr) {
      return '';
    }
    if (expr.type === 'helper') {
      var args = [];
      for (var i = 0; i < expr.args.length; i++) {
        args.push(resolveArg(expr.args[i], scope));
      }
      return resolveHelper(expr.name, args, scope);
    }
    if (expr.type === 'path') {
      var value = resolvePath(expr.path, scope.context, scope.root);
      if (value !== undefined) {
        return value;
      }
      return resolveHelper(expr.path, [], scope);
    }
    return '';
  }

  function renderNodes(nodes, scope) {
    var out = '';
    for (var i = 0; i < nodes.length; i++) {
      var node = nodes[i];
      if (node.type === 'text') {
        out += node.value;
      } else if (node.type === 'expr') {
        out += escapeHtml(evaluateExpression(node.expr, scope));
      } else if (node.type === 'each') {
        var collection = evaluateExpression(node.expr, scope);
        if (typeof collection === 'function') {
          collection = collection.call(scope.context);
        }
        if (Array.isArray(collection)) {
          for (var j = 0; j < collection.length; j++) {
            out += renderNodes(node.children, {
              context: collection[j],
              root: scope.root
            });
          }
        } else if (collection && typeof collection === 'object') {
          var keys = Object.keys(collection);
          for (var k = 0; k < keys.length; k++) {
            out += renderNodes(node.children, {
              context: collection[keys[k]],
              root: scope.root
            });
          }
        }
      }
    }
    return out;
  }

  var ResumeTemplate = {
    registerHelper: function registerHelper(name, fn) {
      if (name && typeof name === 'object') {
        Object.keys(name).forEach(function (key) {
          helpers[key] = name[key];
        });
        return;
      }
      helpers[name] = fn;
    },
    unregisterHelper: function unregisterHelper(name) {
      delete helpers[name];
    },
    compile: function compile(template) {
      var ast = parseTemplate(String(template));
      return function render(context) {
        var rootContext = context || {};
        return renderNodes(ast, { context: rootContext, root: rootContext });
      };
    }
  };

  root.ResumeTemplate = ResumeTemplate;
})(typeof window !== 'undefined' ? window : this);
