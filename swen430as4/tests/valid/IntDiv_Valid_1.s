
	.text
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	movq $10, %rax
	movq $2, %rbx
	movq %rax, %rax
	cqto
	idivq %rbx
	movq %rax, %rax
	movq $5, %rbx
	cmpq %rax, %rbx
	jnz label293
	movq $1, %rax
	jmp label294
label293:
	movq $0, %rax
label294:
	movq %rax, %rdi
	call assertion
	movq $10, %rax
	movq $3, %rbx
	movq %rax, %rax
	cqto
	idivq %rbx
	movq %rax, %rax
	movq $3, %rbx
	cmpq %rax, %rbx
	jnz label295
	movq $1, %rax
	jmp label296
label295:
	movq $0, %rax
label296:
	movq %rax, %rdi
	call assertion
	movq $-10, %rax
	movq $3, %rbx
	movq %rax, %rax
	cqto
	idivq %rbx
	movq %rax, %rax
	movq $-3, %rbx
	cmpq %rax, %rbx
	jnz label297
	movq $1, %rax
	jmp label298
label297:
	movq $0, %rax
label298:
	movq %rax, %rdi
	call assertion
	movq $1, %rax
	movq $4, %rbx
	movq %rax, %rax
	cqto
	idivq %rbx
	movq %rax, %rax
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label299
	movq $1, %rax
	jmp label300
label299:
	movq $0, %rax
label300:
	movq %rax, %rdi
	call assertion
label292:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
