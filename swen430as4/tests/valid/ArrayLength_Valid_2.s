
	.text
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label385
	movq $1, %rax
	jmp label386
label385:
	movq $0, %rax
label386:
	movq %rax, %rdi
	call assertion
	movq $1, %rbx
	cmpq %rax, %rbx
	jnz label387
	movq $1, %rax
	jmp label388
label387:
	movq $0, %rax
label388:
	movq %rax, %rdi
	call assertion
	movq $2, %rbx
	cmpq %rax, %rbx
	jnz label389
	movq $1, %rax
	jmp label390
label389:
	movq $0, %rax
label390:
	movq %rax, %rdi
	call assertion
	movq $3, %rbx
	cmpq %rax, %rbx
	jnz label391
	movq $1, %rax
	jmp label392
label391:
	movq $0, %rax
label392:
	movq %rax, %rdi
	call assertion
label384:
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
